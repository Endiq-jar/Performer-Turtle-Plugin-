package com.turtle.performer.mesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deduplicates vertices so shared corners between adjacent greedy-mesh quads are only stored
 * once, and produces an index buffer pointing back into the compacted vertex list.
 *
 * A "vertex" here is a fixed-stride group of floats: position(3) + normal(3) + uv(2) = 8 floats,
 * matching {@link ChunkMeshBuilder}'s output layout.
 */
public class VertexDeduplicator {
    public static final int STRIDE = 8; // x,y,z, nx,ny,nz, u,v

    public static final class Result {
        public final float[] vertices; // deduplicated, tightly packed
        public final int[] indices;    // one per original vertex, into `vertices`

        Result(float[] vertices, int[] indices) {
            this.vertices = vertices;
            this.indices = indices;
        }
    }

    /**
     * @param in vertex data laid out as consecutive STRIDE-float vertices (as emitted by the mesh
     *           builder, still with duplicates at shared quad edges).
     */
    public Result dedupe(float[] in) {
        if (in.length % STRIDE != 0) {
            throw new IllegalArgumentException("vertex buffer length must be a multiple of " + STRIDE);
        }
        int count = in.length / STRIDE;

        Map<Long, Integer> firstIndexByHash = new HashMap<>(count * 2);
        Map<Long, List<Integer>> bucket = new HashMap<>(count * 2);
        List<Float> outVertices = new ArrayList<>(in.length);
        int[] indices = new int[count];
        int nextOut = 0;

        for (int v = 0; v < count; v++) {
            int base = v * STRIDE;
            long hash = hashVertex(in, base);

            Integer found = null;
            List<Integer> candidates = bucket.get(hash);
            if (candidates != null) {
                for (int candidateOutIndex : candidates) {
                    if (equalsVertex(in, base, outVertices, candidateOutIndex * STRIDE)) {
                        found = candidateOutIndex;
                        break;
                    }
                }
            }

            if (found != null) {
                indices[v] = found;
            } else {
                int outIndex = nextOut++;
                for (int k = 0; k < STRIDE; k++) outVertices.add(in[base + k]);
                bucket.computeIfAbsent(hash, h -> new ArrayList<>()).add(outIndex);
                indices[v] = outIndex;
                firstIndexByHash.put(hash, outIndex);
            }
        }

        float[] verts = new float[outVertices.size()];
        for (int i = 0; i < verts.length; i++) verts[i] = outVertices.get(i);
        return new Result(verts, indices);
    }

    // Backwards-compatible entry point kept for callers that only want raw float dedup
    // (e.g. quick unit tests) without needing an index buffer.
    public float[] deduplicate(float[] in) {
        Result r = dedupe(in);
        return r.vertices;
    }

    private long hashVertex(float[] arr, int base) {
        long h = 1125899906842597L;
        for (int k = 0; k < STRIDE; k++) {
            // Quantize to avoid float jitter creating spurious duplicates/misses.
            int bits = Float.floatToIntBits(Math.round(arr[base + k] * 1024f) / 1024f);
            h = 31 * h + bits;
        }
        return h;
    }

    private boolean equalsVertex(float[] a, int aBase, List<Float> b, int bBase) {
        for (int k = 0; k < STRIDE; k++) {
            if (Float.compare(a[aBase + k], b.get(bBase + k)) != 0) return false;
        }
        return true;
    }
}
