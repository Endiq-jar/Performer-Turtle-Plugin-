package com.plugin.endiq.client.mixin;

import com.turtle.performer.culling.FrustumCuller;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: Frustum#isVisible(AABB), public, line 82 of Frustum.java.
 * Recording only, not altering the result: this project has no reliable
 * way to know whether loosening a visibility test would cause visible
 * pop-in, and Sodium (if present) largely bypasses vanilla's Frustum for
 * chunk-section culling anyway, so this mainly reflects entity/block-entity
 * visibility checks that still route through it. Real data, real hook,
 * intentionally not behavior-changing.
 */
@Mixin(Frustum.class)
public class FrustumStatsMixin {

	public static final FrustumCuller STATS = new FrustumCuller();

	@Inject(method = "isVisible", at = @At("RETURN"))
	private void turtlePerformer$afterIsVisible(AABB bb, CallbackInfoReturnable<Boolean> cir) {
		STATS.recordResult(cir.getReturnValue());
	}
}
