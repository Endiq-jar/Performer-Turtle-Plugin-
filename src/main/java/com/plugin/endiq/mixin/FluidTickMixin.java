package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.FluidTickOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: ServerLevel#tickFluid(BlockPos, Fluid) - private method, real
 * per-position fluid tick entry point (line 807 of ServerLevel.java).
 * Cancellable HEAD injection using FluidTickOptimizer's stateless modulo
 * sampling (safe against permanent-freeze bugs, unlike a set-based dedupe
 * that's never reset). Default interval=1 means this cancels nothing until
 * FluidTickMixin.OPTIMIZER.setInterval(n) is called with n > 1.
 */
@Mixin(ServerLevel.class)
public class FluidTickMixin {

	public static final FluidTickOptimizer OPTIMIZER = new FluidTickOptimizer();
	private static boolean initialized = false;

	@Inject(method = "tickFluid", at = @At("HEAD"), cancellable = true)
	private void turtlePerformer$onTickFluid(BlockPos pos, Fluid type, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		ServerLevel self = (ServerLevel) (Object) this;
		if (!OPTIMIZER.shouldTick(self.getGameTime(), pos.asLong())) {
			ci.cancel();
		}
	}
}
