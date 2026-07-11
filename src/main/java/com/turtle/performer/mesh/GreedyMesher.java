package com.turtle.performer.mesh;

import java.util.ArrayList;
import java.util.List;

/**
 * Real greedy meshing for a cubic chunk section (e.g. 16x16x16 blocks).
 * Merges coplanar, same-material faces into the fewest possible rectangular quads,
 * following the classic "binary greedy mesher" sweep (Mikola Lysenko's algorithm,
 * generalized to arbitrary per-voxel material ids instead of plain booleans).
 *
 * Input: a flat voxel grid `int[sizeX*sizeY*sizeZ]` where 0 = air/empty and any other
 * value is a block/material id (so faces of different materials are never merged together).
 * Output: a list of merged quads, one per visible, maximal rectangular face.
 */
public class GreedyMesher {

    public static final class Quad {
        public final int x, y, z;      // origin voxel coordinate of the quad
        public final int w, h;         // width/height in voxels along the two in-plane axes
        public final int axis;         // 0 = X, 1 = Y, 2 = Z (the axis the face is perpendicular to)
        public final boolean backFace; // which side of the axis the face points to
        public final int material;

        Quad(int x, int y, int z, int w, int h, int axis, boolean backFace, int material) {
            this.x = x; this.y = y; this.z = z;
            this.w = w; this.h = h;
            this.axis = axis; this.backFace = backFace; this.material = material;
        }
    }

    private final int sizeX, sizeY, sizeZ;

    public GreedyMesher(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    private int at(int[] voxels, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ) return 0;
        return voxels[(z * sizeY + y) * sizeX + x];
    }

    /** Runs greedy meshing across all three axes and both face directions. */
    public List<Quad> mesh(int[] voxels) {
        List<Quad> quads = new ArrayList<>();
        for (int axis = 0; axis < 3; axis++) {
            sweepAxis(voxels, axis, false, quads);
            sweepAxis(voxels, axis, true, quads);
        }
        return quads;
    }

    /** Convenience used elsewhere for cheap "how many voxels got merged" stats. */
    public int mergeFaces(boolean[][] mask) {
        int merged = 0;
        for (boolean[] row : mask) for (boolean b : row) if (b) merged++;
        return merged;
    }

    private void sweepAxis(int[] voxels, int axis, boolean backFace, List<Quad> out) {
        int u = (axis + 1) % 3;
        int v = (axis + 2) % 3;
        int[] dims = {sizeX, sizeY, sizeZ};
        int du = dims[u], dv = dims[v], da = dims[axis];

        int[] mask = new int[du * dv];

        for (int a = 0; a <= da; a++) {
            // Build the 2D slice mask: material id of the face at this layer, or 0 if not visible.
            for (int j = 0; j < dv; j++) {
                for (int i = 0; i < du; i++) {
                    int[] posA = new int[3];
                    int[] posB = new int[3];
                    setCoord(posA, axis, u, v, backFace ? a - 1 : a, i, j);
                    setCoord(posB, axis, u, v, backFace ? a : a - 1, i, j);

                    int matA = at(voxels, posA[0], posA[1], posA[2]);
                    int matB = at(voxels, posB[0], posB[1], posB[2]);

                    // A face is visible where one side is solid and the other is air.
                    if (matA != 0 && matB == 0) {
                        mask[j * du + i] = matA;
                    } else {
                        mask[j * du + i] = 0;
                    }
                }
            }

            // Greedily merge the mask into maximal rectangles.
            for (int j = 0; j < dv; j++) {
                for (int i = 0; i < du; ) {
                    int material = mask[j * du + i];
                    if (material == 0) {
                        i++;
                        continue;
                    }

                    // Extend width along u.
                    int w = 1;
                    while (i + w < du && mask[j * du + i + w] == material) w++;

                    // Extend height along v as far as the full width matches.
                    int h = 1;
                    outer:
                    while (j + h < dv) {
                        for (int k = 0; k < w; k++) {
                            if (mask[(j + h) * du + i + k] != material) break outer;
                        }
                        h++;
                    }

                    int[] origin = new int[3];
                    setCoord(origin, axis, u, v, a, i, j);
                    out.add(new Quad(origin[0], origin[1], origin[2], w, h, axis, backFace, material));

                    // Clear the consumed cells so they aren't merged again.
                    for (int hh = 0; hh < h; hh++) {
                        for (int ww = 0; ww < w; ww++) {
                            mask[(j + hh) * du + i + ww] = 0;
                        }
                    }

                    i += w;
                }
            }
        }
    }

    private void setCoord(int[] pos, int axis, int u, int v, int a, int i, int j) {
        pos[axis] = a;
        pos[u] = i;
        pos[v] = j;
    }
}
