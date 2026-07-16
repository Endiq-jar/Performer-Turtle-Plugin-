package com.turtle.performer.adaptive;

/**
 * Tracks a coarse quality tier driven by measured FPS relative to a target.
 * Previously this class had no initialize() method at all while
 * GameRendererFrameMixin called QUALITY_MANAGER.initialize() every frame -
 * a straight compile error. update(int) was also a no-op; it now actually
 * computes a tier that other systems (AdaptiveParticleManager, particle
 * culling) key off of.
 */
public class AdaptiveQualityManager {
	// 0 = full quality, 1 = reduced, 2 = minimal.
	public static final int TIER_FULL = 0;
	public static final int TIER_REDUCED = 1;
	public static final int TIER_MINIMAL = 2;

	private int targetFps = 60;
	private int tier = TIER_FULL;

	public void initialize() {
		tier = TIER_FULL;
	}

	public void update(int fps) {
		if (fps <= 0) {
			return;
		}
		if (fps < targetFps * 0.5) {
			tier = TIER_MINIMAL;
		} else if (fps < targetFps * 0.85) {
			tier = TIER_REDUCED;
		} else if (fps >= targetFps * 0.95) {
			tier = TIER_FULL;
		}
		// fps between 0.85x and 0.95x of target: hold current tier
		// (hysteresis, avoids flapping between tiers every frame).
	}

	public int getQualityTier() {
		return tier;
	}

	public int getTargetFps() {
		return targetFps;
	}

	public void setTargetFps(int fps) {
		targetFps = Math.max(1, fps);
	}
}
