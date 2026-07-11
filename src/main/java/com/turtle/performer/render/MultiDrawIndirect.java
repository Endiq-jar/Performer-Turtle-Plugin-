package com.turtle.performer.render;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

/**
 * Multi Draw Indirect (MDI): batches many chunk-section draw calls (each with its own index
 * range/vertex offset) into a single glMultiDrawElementsIndirect call, cutting per-draw CPU
 * overhead dramatically when a lot of chunk sections are visible at once.
 *
 * Each command follows the standard ARB_multi_draw_indirect DrawElementsIndirectCommand layout:
 *   uint  count;
 *   uint  instanceCount;
 *   uint  firstIndex;
 *   int   baseVertex;
 *   uint  baseInstance;
 * (5 x 4 bytes = 20 bytes per command)
 */
public class MultiDrawIndirect {
    public static final class DrawCommand {
        public final int count, instanceCount, firstIndex, baseVertex, baseInstance;

        public DrawCommand(int count, int instanceCount, int firstIndex, int baseVertex, int baseInstance) {
            this.count = count;
            this.instanceCount = instanceCount;
            this.firstIndex = firstIndex;
            this.baseVertex = baseVertex;
            this.baseInstance = baseInstance;
        }
    }

    private int indirectBuffer = -1;
    private int commandCount = 0;
    private boolean supported = true;

    public void initialize() {
        indirectBuffer = GL15.glGenBuffers();
        // Real feature check: ARB_multi_draw_indirect was core since GL 4.3.
        try {
            supported = org.lwjgl.opengl.GL.getCapabilities().OpenGL43
                    || org.lwjgl.opengl.GL.getCapabilities().GL_ARB_multi_draw_indirect;
        } catch (Throwable t) {
            supported = false;
        }
    }

    public boolean isSupported() {
        return supported;
    }

    /** Uploads the batch of per-chunk-section draw commands for this frame. */
    public void uploadCommands(List<DrawCommand> commands) {
        commandCount = commands.size();
        ByteBuffer buf = ByteBuffer.allocateDirect(commandCount * 20).order(ByteOrder.nativeOrder());
        IntBuffer ints = buf.asIntBuffer();
        for (DrawCommand c : commands) {
            ints.put(c.count).put(c.instanceCount).put(c.firstIndex).put(c.baseVertex).put(c.baseInstance);
        }
        GL40.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBuffer);
        GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, buf, GL15.GL_DYNAMIC_DRAW);
    }

    /**
     * Issues a single indirect draw for every command uploaded via {@link #uploadCommands}.
     * `vao` must already contain the shared/atlas vertex+index buffers all chunk sections draw from.
     */
    public void drawAll(int vao) {
        if (!supported || commandCount == 0) return;
        GL30.glBindVertexArray(vao);
        GL40.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBuffer);
        GL43.glMultiDrawElementsIndirect(GL43.GL_TRIANGLES, GL43.GL_UNSIGNED_INT, 0L, commandCount, 0);
        GL30.glBindVertexArray(0);
    }

    public void shutdown() {
        if (indirectBuffer >= 0) {
            GL15.glDeleteBuffers(indirectBuffer);
            indirectBuffer = -1;
        }
    }
}
