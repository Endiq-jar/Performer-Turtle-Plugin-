package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.ObserverOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: ObserverBlock#updateNeighborsInFront(Level, BlockPos, BlockState),
 * protected, line 86 of ObserverBlock.java - this is the real pulse-dispatch
 * method. Cancellable HEAD injection using ObserverOptimizer.tryPulse(),
 * which defaults to minPulseInterval=2: an observer can pulse at most once
 * every 2 ticks. Vanilla observers can already only pulse once per redstone
 * update cycle in practice, so this is a mild, low-risk default.
 */
@Mixin(ObserverBlock.class)
public class ObserverUpdateMixin {

	public static final ObserverOptimizer OPTIMIZER = new ObserverOptimizer();
	private static boolean initialized = false;

	@Inject(method = "updateNeighborsInFront", at = @At("HEAD"), cancellable = true)
	private void turtlePerformer$onUpdateNeighbors(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		if (!OPTIMIZER.tryPulse(pos.asLong(), level.getGameTime())) {
			ci.cancel();
		}
	}
}
