package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.RandomTickOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: ServerLevel#tickBlock(BlockPos, Block) - private method, the
 * actual per-position random-tick dispatch entry (line 815 of
 * ServerLevel.java), called from tickChunk. This is the correct, precise
 * hook for random-tick throttling (RandomTickChunkMixin, added earlier,
 * only observes the whole-chunk call and never cancelled anything - this
 * mixin is the one that actually skips individual block random ticks).
 * Default interval=1 means vanilla-equivalent until
 * RandomTickBlockMixin.OPTIMIZER.setInterval(n) is raised above 1.
 */
@Mixin(ServerLevel.class)
public class RandomTickBlockMixin {

	public static final RandomTickOptimizer OPTIMIZER = new RandomTickOptimizer();
	private static boolean initialized = false;

	@Inject(method = "tickBlock", at = @At("HEAD"), cancellable = true)
	private void turtlePerformer$onTickBlock(BlockPos pos, Block type, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		ServerLevel self = (ServerLevel) (Object) this;
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		if (!OPTIMIZER.shouldTick(self.getGameTime(), chunkX, chunkZ)) {
			ci.cancel();
		}
	}
}
