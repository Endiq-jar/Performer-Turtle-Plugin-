package com.turtle.performer.mesh;

/**
 * Post-transform vertex cache optimization: reorders a triangle index buffer so that
 * consecutive triangles reuse vertices that are still hot in the GPU's small FIFO
 * post-transform cache, cutting down redundant vertex shader invocations.
 *
 * This is a linear-speed greedy optimizer (similar in spirit to Forsyth's algorithm, without
 * the score-decay tables) that:
 *   1. Builds an adjacency list: for every vertex, which triangles reference it.
 *   2. Walks triangles in original order, but whenever the *next* triangle would cause a cache
 *      miss on all three vertices, instead greedily picks an unvisited triangle that shares at
 *      least one vertex with the current FIFO cache.
 */
public class VertexBufferOptimizer {
    private static final int CACHE_SIZE = 24; // typical small post-T&L vertex cache size

    /** Optimizes in place order of triangles (index triples) for better vertex cache reuse. */
    public int[] optimize(int[] indices) {
        if (indices.length % 3 != 0) {
            throw new IllegalArgumentException("index buffer length must be a multiple of 3");
        }
        int triCount = indices.length / 3;
        if (triCount <= 1) return indices;

        int vertexCount = 0;
        for (int idx : indices) vertexCount = Math.max(vertexCount, idx + 1);

        // vertex -> list of triangle indices referencing it
        java.util.List<java.util.List<Integer>> vertexTriangles = new java.util.ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) vertexTriangles.add(new java.util.ArrayList<>());
        for (int t = 0; t < triCount; t++) {
            for (int c = 0; c < 3; c++) {
                vertexTriangles.get(indices[t * 3 + c]).add(t);
            }
        }

        boolean[] emitted = new boolean[triCount];
        int[] out = new int[indices.length];
        int outTri = 0;

        java.util.ArrayDeque<Integer> fifo = new java.util.ArrayDeque<>(CACHE_SIZE);

        int nextScanTri = 0;
        while (outTri < triCount) {
            Integer candidate = null;

            // Prefer a not-yet-emitted triangle that shares a vertex with the current cache.
            for (int cachedVertex : fifo) {
                for (int t : vertexTriangles.get(cachedVertex)) {
                    if (!emitted[t]) {
                        candidate = t;
                        break;
                    }
                }
                if (candidate != null) break;
            }

            // Nothing cache-adjacent left; fall back to the next unemitted triangle in
            // original submission order (keeps unrelated mesh islands from thrashing).
            if (candidate == null) {
                while (nextScanTri < triCount && emitted[nextScanTri]) nextScanTri++;
                candidate = nextScanTri;
            }

            emitted[candidate] = true;
            for (int c = 0; c < 3; c++) {
                int v = indices[candidate * 3 + c];
                out[outTri * 3 + c] = v;

                fifo.remove(v); // move-to-front semantics
                fifo.addFirst(v);
                while (fifo.size() > CACHE_SIZE) fifo.removeLast();
            }
            outTri++;
        }

        return out;
    }
}
