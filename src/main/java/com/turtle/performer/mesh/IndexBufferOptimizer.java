package com.turtle.performer.mesh;

/**
 * Picks the smallest index type that can represent a mesh's vertex count, and packs the index
 * buffer down accordingly. Most chunk section meshes have well under 65536 vertices, so this
 * usually lets the GPU driver use GL_UNSIGNED_SHORT indices instead of GL_UNSIGNED_INT, halving
 * index buffer bandwidth. Delegates cache-friendly ordering to {@link VertexBufferOptimizer}.
 */
public class IndexBufferOptimizer {
    public enum IndexType { UNSIGNED_BYTE, UNSIGNED_SHORT, UNSIGNED_INT }

    public static final class Result {
        public final int[] indices;   // vertex-cache optimized, still widened to int for convenience
        public final IndexType type;  // smallest GL index type that fits

        Result(int[] indices, IndexType type) {
            this.indices = indices;
            this.type = type;
        }
    }

    private final VertexBufferOptimizer cacheOptimizer = new VertexBufferOptimizer();

    public Result optimize(int[] idx, int vertexCount) {
        int[] reordered = cacheOptimizer.optimize(idx);
        IndexType type;
        if (vertexCount <= 256) {
            type = IndexType.UNSIGNED_BYTE;
        } else if (vertexCount <= 65536) {
            type = IndexType.UNSIGNED_SHORT;
        } else {
            type = IndexType.UNSIGNED_INT;
        }
        return new Result(reordered, type);
    }

    // Backwards compatible signature (no vertex-count context, so it can only optimize order).
    public int[] optimize(int[] idx) {
        return cacheOptimizer.optimize(idx);
    }
}
