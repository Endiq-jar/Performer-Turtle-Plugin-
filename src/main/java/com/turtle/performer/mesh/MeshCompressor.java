package com.turtle.performer.mesh;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * Compresses chunk mesh vertex data for storage in the mesh cache / for network transfer.
 *
 * Two stages:
 *  1. Quantization: position components (assumed to be in local chunk space, 0..16) are packed
 *     into unsigned shorts instead of 32-bit floats; normals are packed into a single byte
 *     (axis-aligned mesh normals only need 6 discrete directions); UVs are packed into shorts.
 *     This alone shrinks an 8-float (32 byte) vertex down to 13 bytes.
 *  2. DEFLATE: the quantized byte stream is further compressed with java.util.zip, which does
 *     well here because chunk meshes have a lot of repeated/near-repeated vertex patterns.
 */
public class MeshCompressor {
    private static final float POSITION_SCALE = 4096.0f / 16.0f; // 16 = max local chunk extent
    private static final float UV_SCALE = 65535.0f;

    /** Quantizes + DEFLATE-compresses an interleaved (x,y,z,nx,ny,nz,u,v) vertex buffer. */
    public byte[] compress(float[] vertices) {
        if (vertices.length % VertexDeduplicator.STRIDE != 0) {
            throw new IllegalArgumentException("vertex buffer length must be a multiple of " + VertexDeduplicator.STRIDE);
        }
        int count = vertices.length / VertexDeduplicator.STRIDE;
        ByteBuffer packed = ByteBuffer.allocate(count * 13).order(ByteOrder.LITTLE_ENDIAN);

        for (int v = 0; v < count; v++) {
            int base = v * VertexDeduplicator.STRIDE;
            packed.putShort((short) clampUnsigned(vertices[base] * POSITION_SCALE));
            packed.putShort((short) clampUnsigned(vertices[base + 1] * POSITION_SCALE));
            packed.putShort((short) clampUnsigned(vertices[base + 2] * POSITION_SCALE));
            packed.put(packNormal(vertices[base + 3], vertices[base + 4], vertices[base + 5]));
            packed.putShort((short) clampUnsigned(vertices[base + 6] * UV_SCALE));
            packed.putShort((short) clampUnsigned(vertices[base + 7] * UV_SCALE));
        }

        return deflate(packed.array());
    }

    /** Reverses {@link #compress}, restoring the original interleaved float layout. */
    public float[] decompress(byte[] compressed) {
        byte[] packed = inflate(compressed);
        int count = packed.length / 13;
        ByteBuffer buf = ByteBuffer.wrap(packed).order(ByteOrder.LITTLE_ENDIAN);
        float[] out = new float[count * VertexDeduplicator.STRIDE];

        for (int v = 0; v < count; v++) {
            int base = v * VertexDeduplicator.STRIDE;
            out[base] = (buf.getShort() & 0xFFFF) / POSITION_SCALE;
            out[base + 1] = (buf.getShort() & 0xFFFF) / POSITION_SCALE;
            out[base + 2] = (buf.getShort() & 0xFFFF) / POSITION_SCALE;
            float[] normal = unpackNormal(buf.get());
            out[base + 3] = normal[0];
            out[base + 4] = normal[1];
            out[base + 5] = normal[2];
            out[base + 6] = (buf.getShort() & 0xFFFF) / UV_SCALE;
            out[base + 7] = (buf.getShort() & 0xFFFF) / UV_SCALE;
        }
        return out;
    }

    // Kept for callers that just want to compress arbitrary already-packed bytes
    // (e.g. serialized index buffers) without the vertex-specific quantization above.
    public byte[] compressRaw(byte[] d) {
        return deflate(d);
    }

    public byte[] decompressRaw(byte[] d) {
        return inflate(d);
    }

    private static int clampUnsigned(float v) {
        if (v < 0) return 0;
        if (v > 65535) return 65535;
        return Math.round(v);
    }

    // Axis-aligned chunk mesh normals only ever point along +-X/+-Y/+-Z, so we store a
    // 0..5 face index instead of 3 floats.
    private static byte packNormal(float nx, float ny, float nz) {
        if (nx > 0.5f) return 0;
        if (nx < -0.5f) return 1;
        if (ny > 0.5f) return 2;
        if (ny < -0.5f) return 3;
        if (nz > 0.5f) return 4;
        return 5;
    }

    private static float[] unpackNormal(byte packed) {
        switch (packed) {
            case 0: return new float[]{1, 0, 0};
            case 1: return new float[]{-1, 0, 0};
            case 2: return new float[]{0, 1, 0};
            case 3: return new float[]{0, -1, 0};
            case 4: return new float[]{0, 0, 1};
            default: return new float[]{0, 0, -1};
        }
    }

    private static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(32, data.length / 2));
            try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
                dos.write(data);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("mesh compression failed", e);
        } finally {
            deflater.end();
        }
    }

    private static byte[] inflate(byte[] data) {
        Inflater inflater = new Inflater();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(32, data.length * 2));
            try (InflaterOutputStream ios = new InflaterOutputStream(baos, inflater)) {
                ios.write(data);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("mesh decompression failed", e);
        } finally {
            inflater.end();
        }
    }
}
