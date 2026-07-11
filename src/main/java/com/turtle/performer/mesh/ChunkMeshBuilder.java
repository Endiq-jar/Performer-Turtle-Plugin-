package com.turtle.performer.mesh;

import java.util.List;

/**
 * End-to-end pipeline that turns a raw voxel grid for a chunk section into optimized,
 * render-ready (and cache-storable) mesh data:
 *
 *   voxels -> GreedyMesher (merge faces) -> emit per-quad vertices
 *          -> VertexDeduplicator (shared-edge dedup + index buffer)
 *          -> IndexBufferOptimizer (vertex-cache order + smallest index type)
 *          -> MeshCompressor (quantize + deflate, for the mesh cache / disk / network)
 */
public class ChunkMeshBuilder {

    public static final class BuiltMesh {
        public final float[] vertices;               // deduplicated, STRIDE-float interleaved
        public final int[] indices;                   // vertex-cache optimized
        public final IndexBufferOptimizer.IndexType indexType;
        public final byte[] compressed;                // for ChunkMeshCache / disk storage
        public final int quadCount;

        BuiltMesh(float[] vertices, int[] indices, IndexBufferOptimizer.IndexType indexType,
                  byte[] compressed, int quadCount) {
            this.vertices = vertices;
            this.indices = indices;
            this.indexType = indexType;
            this.compressed = compressed;
            this.quadCount = quadCount;
        }
    }

    private final int sizeX, sizeY, sizeZ;
    private final GreedyMesher greedyMesher;
    private final VertexDeduplicator deduplicator = new VertexDeduplicator();
    private final IndexBufferOptimizer indexOptimizer = new IndexBufferOptimizer();
    private final MeshCompressor compressor = new MeshCompressor();

    public ChunkMeshBuilder(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.greedyMesher = new GreedyMesher(sizeX, sizeY, sizeZ);
    }

    /** Convenience constructor for a standard 16^3 Minecraft chunk section. */
    public ChunkMeshBuilder() {
        this(16, 16, 16);
    }

    /**
     * @param voxels flat sizeX*sizeY*sizeZ array of material ids, 0 = air.
     */
    public BuiltMesh build(int[] voxels) {
        List<GreedyMesher.Quad> quads = greedyMesher.mesh(voxels);

        float[] rawVertices = new float[quads.size() * 4 * VertexDeduplicator.STRIDE];
        int cursor = 0;
        for (GreedyMesher.Quad q : quads) {
            cursor = emitQuad(rawVertices, cursor, q);
        }

        VertexDeduplicator.Result dedupResult = deduplicator.dedupe(rawVertices);

        // Rebuild triangle index list (2 tris per quad) referencing the deduped vertex ids.
        // dedupResult.indices maps "original raw vertex slot" -> "deduped vertex id", and raw
        // vertices were emitted 4-per-quad in a consistent winding, so triangulate per quad.
        int quadCount = quads.size();
        int[] triangles = new int[quadCount * 6];
        for (int q = 0; q < quadCount; q++) {
            int v0 = dedupResult.indices[q * 4];
            int v1 = dedupResult.indices[q * 4 + 1];
            int v2 = dedupResult.indices[q * 4 + 2];
            int v3 = dedupResult.indices[q * 4 + 3];
            int t = q * 6;
            triangles[t] = v0; triangles[t + 1] = v1; triangles[t + 2] = v2;
            triangles[t + 3] = v0; triangles[t + 4] = v2; triangles[t + 5] = v3;
        }

        int vertexCount = dedupResult.vertices.length / VertexDeduplicator.STRIDE;
        IndexBufferOptimizer.Result optimized = indexOptimizer.optimize(triangles, vertexCount);
        byte[] compressed = compressor.compress(dedupResult.vertices);

        return new BuiltMesh(dedupResult.vertices, optimized.indices, optimized.type, compressed, quadCount);
    }

    private int emitQuad(float[] out, int cursor, GreedyMesher.Quad q) {
        float[] normal = normalFor(q.axis, q.backFace);
        float[][] corners = cornersFor(q);

        // uv scaled to the quad's actual size so textures/atlases tile correctly across merges.
        float[][] uvs = {{0, 0}, {q.w, 0}, {q.w, q.h}, {0, q.h}};

        for (int c = 0; c < 4; c++) {
            out[cursor++] = corners[c][0];
            out[cursor++] = corners[c][1];
            out[cursor++] = corners[c][2];
            out[cursor++] = normal[0];
            out[cursor++] = normal[1];
            out[cursor++] = normal[2];
            out[cursor++] = uvs[c][0];
            out[cursor++] = uvs[c][1];
        }
        return cursor;
    }

    private float[] normalFor(int axis, boolean backFace) {
        float sign = backFace ? -1f : 1f;
        switch (axis) {
            case 0: return new float[]{sign, 0, 0};
            case 1: return new float[]{0, sign, 0};
            default: return new float[]{0, 0, sign};
        }
    }

    // Produces the 4 world-space corners of a quad, winding consistently CCW when viewed
    // from the direction the face normal points.
    private float[][] cornersFor(GreedyMesher.Quad q) {
        int u = (q.axis + 1) % 3;
        int v = (q.axis + 2) % 3;

        float[] base = {q.x, q.y, q.z};

        float[] du = new float[3];
        float[] dv = new float[3];
        du[u] = q.w;
        dv[v] = q.h;

        float[] p0 = base.clone();
        float[] p1 = add(base, du);
        float[] p2 = add(add(base, du), dv);
        float[] p3 = add(base, dv);

        if (q.backFace) {
            // reverse winding so the face still points outward (toward -axis)
            return new float[][]{p0, p3, p2, p1};
        }
        return new float[][]{p0, p1, p2, p3};
    }

    private float[] add(float[] a, float[] b) {
        return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }
}
