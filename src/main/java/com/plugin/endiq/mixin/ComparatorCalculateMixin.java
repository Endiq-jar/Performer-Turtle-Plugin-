package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.ComparatorOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: ComparatorBlock#calculateOutputSignal(Level, BlockPos, BlockState)
 * - private, line 71 of ComparatorBlock.java, returns int. Confirmed real,
 * replaces the earlier weaker DiodeBlock#onPlace guess from a previous
 * session. Still not cancelled/short-circuited: doing that safely requires
 * knowing whether a comparator's redstone inputs actually changed since the
 * last calculation, which needs neighbor-update tracking infrastructure this
 * project doesn't have. Recording the real computed value via
 * ComparatorOptimizer.outputChanged() so that data exists if that tracking
 * gets built later.
 */
@Mixin(ComparatorBlock.class)
public class ComparatorCalculateMixin {

	public static final ComparatorOptimizer OPTIMIZER = new ComparatorOptimizer();
	private static boolean initialized = false;

	@Inject(method = "calculateOutputSignal", at = @At("RETURN"))
	private void turtlePerformer$afterCalculate(Level level, BlockPos pos, BlockState state,
			CallbackInfoReturnable<Integer> cir) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		OPTIMIZER.outputChanged(pos.asLong(), cir.getReturnValue());
	}
}
