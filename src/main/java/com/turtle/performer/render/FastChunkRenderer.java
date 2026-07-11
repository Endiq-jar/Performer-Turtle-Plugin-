package com.turtle.performer.render;

import com.plugin.endiq.performance.render.RendererManager;
import com.turtle.performer.mesh.ChunkMeshBuilder;
import com.turtle.performer.chunks.ChunkMeshCache;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Top-level fast chunk rendering path. Ties together:
 *   - RendererManager: picks Vulkan if usable, else OpenGL, once per session.
 *   - ChunkMeshBuilder: greedy meshing + dedup + vertex-cache order + compression per section.
 *   - RenderRegionBatcher / MultiDrawIndirect: batches many sections into one draw call.
 *   - GpuInstancing: used for repeated small props (e.g. foliage) within a section.
 *
 * This class owns the per-section GPU buffer objects and the region batcher; it is the single
 * entry point the rest of the mod should call to (re)build and draw chunk geometry.
 */
public class FastChunkRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("performer-turtle-plugin");

    private static final class GpuSection {
        int vao, vbo, ibo;
        int indexCount;
        ChunkMeshBuilder.BuiltMesh mesh;
    }

    private final ChunkMeshBuilder meshBuilder = new ChunkMeshBuilder();
    private final ChunkMeshCache meshCache = new ChunkMeshCache();
    private final RenderRegionBatcher regionBatcher = new RenderRegionBatcher();
    private final GpuInstancing instancing = new GpuInstancing();
    private final Map<Long, GpuSection> sections = new HashMap<>();

    private boolean initialized = false;

    public void initialize() {
        if (initialized) return;
        RendererManager.get().init();
        regionBatcher.initialize();
        initialized = true;
        LOGGER.info("FastChunkRenderer initialized using backend={}, MDI supported={}",
                RendererManager.get().active().name(), regionBatcher.isMdiSupported());
    }

    /**
     * (Re)builds and uploads GPU buffers for one chunk section. Cheap to call often - the mesh
     * cache means unchanged sections skip the greedy-mesh + dedup work entirely.
     *
     * @param sectionKey stable id for this section (e.g. packed chunk coords + Y index)
     * @param voxels     flat 16x16x16 material-id array for this section
     */
    public void uploadSection(long sectionKey, int[] voxels) {
        ChunkMeshBuilder.BuiltMesh cached = (ChunkMeshBuilder.BuiltMesh) meshCache.get(sectionKey);
        ChunkMeshBuilder.BuiltMesh mesh = cached != null ? cached : meshBuilder.build(voxels);
        if (cached == null) {
            meshCache.put(sectionKey, mesh);
        }

        GpuSection section = sections.computeIfAbsent(sectionKey, k -> createGpuSection());
        uploadGeometry(section, mesh);

        regionBatcher.addSection(new RenderRegionBatcher.SectionEntry(
                sectionKey, section.indexCount, /*firstIndex*/ 0, /*baseVertex*/ 0));
    }

    public void removeSection(long sectionKey) {
        GpuSection section = sections.remove(sectionKey);
        if (section != null) {
            GL15.glDeleteBuffers(section.vbo);
            GL15.glDeleteBuffers(section.ibo);
            GL30.glDeleteVertexArrays(section.vao);
        }
        regionBatcher.removeSection(sectionKey);
    }

    /** Draws all currently uploaded, batched sections. Falls back to per-section draws if MDI isn't supported. */
    public void renderAll() {
        if (sections.isEmpty()) return;

        if (regionBatcher.isMdiSupported()) {
            // All sections share the same vertex attribute layout; any one VAO's format works,
            // MDI reads offsets per-command so each section's actual buffer range is respected.
            long anyKey = sections.keySet().iterator().next();
            regionBatcher.drawRegion(sections.get(anyKey).vao);
        } else {
            for (GpuSection section : sections.values()) {
                GL30.glBindVertexArray(section.vao);
                GL20.glDrawElements(GL20.GL_TRIANGLES, section.indexCount, GL20.GL_UNSIGNED_INT, 0L);
            }
            GL30.glBindVertexArray(0);
        }
    }

    /** Draws repeated small props (grass, flowers, etc) within a section via GPU instancing. */
    public void renderInstancedProps(int propVao, int propIndexAttribCount, FloatBuffer transforms, int indexCount, int instanceCount) {
        instancing.initialize(propVao, propIndexAttribCount, 16);
        instancing.updateInstances(transforms);
        instancing.drawInstanced(indexCount, instanceCount);
    }

    private GpuSection createGpuSection() {
        GpuSection s = new GpuSection();
        s.vao = GL30.glGenVertexArrays();
        s.vbo = GL15.glGenBuffers();
        s.ibo = GL15.glGenBuffers();

        GL30.glBindVertexArray(s.vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, s.vbo);

        int stride = 8 * Float.BYTES; // position(3) + normal(3) + uv(2)
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL20.GL_FLOAT, false, stride, (long) (3 * Float.BYTES));
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 2, GL20.GL_FLOAT, false, stride, (long) (6 * Float.BYTES));

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, s.ibo);
        GL30.glBindVertexArray(0);
        return s;
    }

    private void uploadGeometry(GpuSection section, ChunkMeshBuilder.BuiltMesh mesh) {
        section.mesh = mesh;
        section.indexCount = mesh.indices.length;

        GL30.glBindVertexArray(section.vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, section.vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.vertices, GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, section.ibo);
        IntBuffer indexBuf = IntBuffer.wrap(mesh.indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuf, GL15.GL_STATIC_DRAW);

        GL30.glBindVertexArray(0);
    }

    public void shutdown() {
        for (GpuSection section : sections.values()) {
            GL15.glDeleteBuffers(section.vbo);
            GL15.glDeleteBuffers(section.ibo);
            GL30.glDeleteVertexArrays(section.vao);
        }
        sections.clear();
        regionBatcher.shutdown();
        instancing.shutdown();
        initialized = false;
    }
}
