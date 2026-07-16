package com.plugin.endiq.mixin;

import com.turtle.performer.chunks.ChunkManager;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Real target confirmed against current CaffeineMC/lithium source
 * (mixin.util.chunk_status_tracking.LevelChunkMixin), which hooks this
 * identical real vanilla method - net.minecraft.world.level.chunk
 * .LevelChunk#setFullStatus(Supplier) - to detect chunks becoming fully
 * accessible. Lithium's own version feeds a full ChunkStatusTracker system
 * used elsewhere for query optimizations; this project doesn't have that
 * infrastructure, so this just records the real event count via
 * @At("RETURN") instead of discarding the signal like before.
 */
@Mixin(LevelChunk.class)
public class ChunkAccessibleActivityMixin {

	public static final ChunkManager MANAGER = new ChunkManager();
	private static boolean initialized = false;

	@Inject(method = "setFullStatus(Ljava/util/function/Supplier;)V", at = @At("RETURN"))
	private void turtlePerformer$onSetFullStatus(Supplier<FullChunkStatus> supplier, CallbackInfo ci) {
		if (!initialized) {
			MANAGER.initialize();
			initialized = true;
		}
		if (supplier != null) {
			MANAGER.recordChunkAccessible();
		}
	}
}
