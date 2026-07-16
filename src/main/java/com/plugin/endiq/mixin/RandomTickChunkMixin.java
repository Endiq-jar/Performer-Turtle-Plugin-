package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.RandomTickOptimizer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real mixin target confirmed against Lithium 0.25.2 for MC 26.2
 * (mixin.world.chunk_ticking.random_block_ticking.ServerLevelMixin), which
 * targets ServerLevel#tickChunk(LevelChunk, int). This is the actual vanilla
 * random-tick entry point per chunk, confirmed by a currently-shipping mod.
 */
@Mixin(ServerLevel.class)
public class RandomTickChunkMixin {

	public static final RandomTickOptimizer OPTIMIZER = new RandomTickOptimizer();
	private static boolean initialized = false;

	@Inject(method = "tickChunk", at = @At("HEAD"))
	private void turtlePerformer$beforeTickChunk(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		// A full skip requires @Redirect/@ModifyArg on the internal random-tick
		// loop rather than a HEAD injection on the whole chunk (that would skip
		// block-entity ticking too, which is wrong). Left as a hook point
		// for scoping down once the internal loop's structure is confirmed
		// from your local genSources.
	}
}
