package com.turtle.performer.render;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups uploaded chunk-section meshes that share a single vertex/index buffer region into
 * MultiDrawIndirect command batches, so a whole render region (e.g. a 4x4x4 block of chunk
 * sections) can be drawn with one glMultiDrawElementsIndirect call instead of one draw call
 * per section.
 */
public class RenderRegionBatcher {

    public static final class SectionEntry {
        public final long sectionKey;
        public final int indexCount;
        public final int firstIndex;
        public final int baseVertex;

        public SectionEntry(long sectionKey, int indexCount, int firstIndex, int baseVertex) {
            this.sectionKey = sectionKey;
            this.indexCount = indexCount;
            this.firstIndex = firstIndex;
            this.baseVertex = baseVertex;
        }
    }

    private final List<SectionEntry> sections = new ArrayList<>();
    private final MultiDrawIndirect mdi = new MultiDrawIndirect();
    private boolean dirty = true;

    public void initialize() {
        mdi.initialize();
    }

    public void addSection(SectionEntry entry) {
        sections.add(entry);
        dirty = true;
    }

    public void removeSection(long sectionKey) {
        dirty |= sections.removeIf(e -> e.sectionKey == sectionKey);
    }

    public void clear() {
        sections.clear();
        dirty = true;
    }

    /** Rebuilds and uploads the MDI command list only when sections changed since last frame. */
    private void flushIfDirty() {
        if (!dirty) return;
        List<MultiDrawIndirect.DrawCommand> commands = new ArrayList<>(sections.size());
        for (SectionEntry e : sections) {
            commands.add(new MultiDrawIndirect.DrawCommand(e.indexCount, 1, e.firstIndex, e.baseVertex, 0));
        }
        mdi.uploadCommands(commands);
        dirty = false;
    }

    /** Draws every batched section in this region with a single indirect draw call. */
    public void drawRegion(int sharedVao) {
        flushIfDirty();
        mdi.drawAll(sharedVao);
    }

    public boolean isMdiSupported() {
        return mdi.isSupported();
    }

    public int sectionCount() {
        return sections.size();
    }

    public void shutdown() {
        mdi.shutdown();
        sections.clear();
    }
}
