package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.RedstoneOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Real target confirmed against Lithium 0.25.2 for MC 26.2
 * (mixin.block.redstone_wire.RedstoneWireEvaluatorMixin), which hooks
 * RedstoneWireEvaluator#getIncomingWireSignal(Level, BlockPos). Tracks
 * whether the computed power actually changed at this position so repeat
 * identical recalculations can be recognized (does not itself cancel/skip
 * vanilla's calculation - see note below).
 */
@Mixin(RedstoneWireEvaluator.class)
public class RedstoneWireMixin {

	public static final RedstoneOptimizer OPTIMIZER = new RedstoneOptimizer();
	private static boolean initialized = false;

	@Inject(method = "getIncomingWireSignal", at = @At("RETURN"))
	private void turtlePerformer$afterSignal(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		// Records the change for anything else that wants to query it via
		// OPTIMIZER.powerChanged(); does not cancel/short-circuit this call
		// since that requires overriding the return value with cir.setReturnValue(...)
		// using our own cached result, which needs cross-checking against
		// RedstoneWireEvaluator's actual field layout first.
		OPTIMIZER.powerChanged(pos.asLong(), cir.getReturnValue());
	}
}
