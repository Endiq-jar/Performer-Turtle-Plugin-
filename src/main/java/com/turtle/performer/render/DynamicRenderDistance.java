package com.turtle.performer.render;

/**
 * Turns AdaptiveQualityManager's FPS-based quality tier into an actual
 * render-distance value. Deliberately does NOT apply a new value every
 * frame - changing render distance reissues chunk loading tickets, which
 * is itself expensive, so this only proposes a change at most once per
 * MIN_INTERVAL_NANOS and only when the tier has actually changed.
 */
public class DynamicRenderDistance {

	private static final long MIN_INTERVAL_NANOS = 5_000_000_000L; // 5s
	private static final int FLOOR = 4;

	private int baseline = -1;
	private int lastAppliedTier = -1;
	private long lastChangeNanos = 0;

	public void initialize() {
		baseline = -1;
		lastAppliedTier = -1;
		lastChangeNanos = 0;
	}

	public void captureBaseline(int currentDistance) {
		if (baseline < 0) {
			baseline = currentDistance;
		}
	}

	/**
	 * Returns the render distance that should be applied right now, or -1
	 * if nothing should change (tier unchanged, or still within the
	 * rate-limit window since the last real change).
	 */
	public int proposeDistance(int tier, long nowNanos) {
		if (baseline < 0) {
			return -1;
		}
		if (tier == lastAppliedTier) {
			return -1;
		}
		if (nowNanos - lastChangeNanos < MIN_INTERVAL_NANOS) {
			return -1;
		}
		int target;
		switch (tier) {
			case 1: target = Math.max(FLOOR, baseline - 2); break;
			case 2: target = Math.max(FLOOR, baseline - 4); break;
			default: target = baseline; break;
		}
		lastAppliedTier = tier;
		lastChangeNanos = nowNanos;
		return target;
	}
}
