package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.CropOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: CropBlock#randomTick(BlockState, ServerLevel, BlockPos,
 * RandomSource), protected, line 85 of CropBlock.java.
 *
 * IMPORTANT behavioral note: we have no confirmed way to read a crop's
 * actual growth-stage/age property generically here (that's a per-block
 * BlockState property whose name varies by crop type, e.g. "age"), so this
 * always passes mature=true to CropOptimizer.shouldGrowthTick(), which
 * applies matureSampleRate (default 8) uniformly to every crop random tick.
 * That is a REAL default behavior change: crops grow roughly 8x less often
 * on average out of the box. Lower CropOptimizer.OPTIMIZER's
 * matureSampleRate to 1 via CropRandomTickMixin.OPTIMIZER.setMatureSampleRate(1)
 * if you want vanilla-identical crop growth speed instead.
 */
@Mixin(CropBlock.class)
public class CropRandomTickMixin {

	public static final CropOptimizer OPTIMIZER = new CropOptimizer();
	private static boolean initialized = false;

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void turtlePerformer$onRandomTick(BlockState state, ServerLevel level, BlockPos pos,
			RandomSource random, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		if (!OPTIMIZER.shouldGrowthTick(level.getGameTime(), pos.asLong(), true)) {
			ci.cancel();
		}
	}
}
