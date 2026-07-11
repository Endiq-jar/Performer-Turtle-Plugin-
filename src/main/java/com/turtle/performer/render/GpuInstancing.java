package com.turtle.performer.render;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;

/**
 * GPU instancing helper: draws many copies of the same mesh (e.g. grass/flower blocks,
 * particles, or repeated foliage models) in a single draw call by uploading a per-instance
 * transform buffer and using glVertexAttribDivisor so the vertex shader advances one instance
 * attribute per instance instead of per vertex.
 */
public class GpuInstancing {
    private int vao = -1;
    private int instanceVbo = -1;
    private int floatsPerInstance;
    private int baseAttribCount;

    /**
     * @param vao existing vertex array object already holding the base mesh's per-vertex attributes
     * @param baseAttribCount how many vertex attribute slots (0..baseAttribCount-1) the mesh already uses,
     *                        so instance attributes are bound starting at baseAttribCount.
     * @param floatsPerInstance size of one instance's data (e.g. 16 for a 4x4 transform matrix)
     */
    public void initialize(int vao, int baseAttribCount, int floatsPerInstance) {
        this.vao = vao;
        this.baseAttribCount = baseAttribCount;
        this.floatsPerInstance = floatsPerInstance;

        GL30.glBindVertexArray(vao);
        instanceVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);

        // A mat4 needs 4 vec4 attribute slots; wire up as many as floatsPerInstance/4 requires,
        // falling back to plain vec4 chunks for arbitrary per-instance payloads.
        int vec4Count = Math.max(1, floatsPerInstance / 4);
        int stride = vec4Count * 4 * Float.BYTES;
        for (int i = 0; i < vec4Count; i++) {
            int attribIndex = baseAttribCount + i;
            GL20.glEnableVertexAttribArray(attribIndex);
            GL20.glVertexAttribPointer(attribIndex, 4, GL20.GL_FLOAT, false, stride, (long) i * 4 * Float.BYTES);
            GL33.glVertexAttribDivisor(attribIndex, 1); // advance once per instance, not per vertex
        }

        GL30.glBindVertexArray(0);
    }

    /** Uploads the latest per-instance data (e.g. model matrices for each visible block/entity). */
    public void updateInstances(FloatBuffer instanceData) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceData, GL15.GL_DYNAMIC_DRAW);
    }

    /** Draws `instanceCount` copies of the mesh currently bound to `vao` using indexed geometry. */
    public void drawInstanced(int indexCount, int instanceCount) {
        if (vao < 0 || instanceCount <= 0) return;
        GL30.glBindVertexArray(vao);
        GL31.glDrawElementsInstanced(GL31.GL_TRIANGLES, indexCount, GL31.GL_UNSIGNED_INT, 0L, instanceCount);
        GL30.glBindVertexArray(0);
    }

    public void shutdown() {
        if (instanceVbo >= 0) {
            GL15.glDeleteBuffers(instanceVbo);
            instanceVbo = -1;
        }
    }
}
