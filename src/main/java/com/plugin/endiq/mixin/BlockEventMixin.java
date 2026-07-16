package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.BlockEventOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: Level#blockEvent(BlockPos, Block, int, int), line 909 of
 * Level.java - the actual dispatch for vanilla "block events" (piston
 * push/retract animation, chest open/close, note block play, etc).
 * Cancellable, using BlockEventOptimizer.shouldDispatch() to dedupe
 * identical repeat events at the same position - real, safe cancellation:
 * dropping an exact duplicate (same type, same data) doesn't change any
 * observable behavior, it just skips redundant network/animation dispatch.
 */
@Mixin(Level.class)
public abstract class BlockEventMixin {

	public static final BlockEventOptimizer OPTIMIZER = new BlockEventOptimizer();
	private static boolean initialized = false;

	@Inject(method = "blockEvent", at = @At("HEAD"), cancellable = true)
	private void turtlePerformer$onBlockEvent(BlockPos pos, Block block, int b0, int b1, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		if (!OPTIMIZER.shouldDispatch(pos.asLong(), b0, b1)) {
			ci.cancel();
		}
	}
}
