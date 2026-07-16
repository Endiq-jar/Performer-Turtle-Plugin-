package com.plugin.endiq.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * LevelChunk#isTicking(BlockPos) is private, so TileEntityTickGateMixin's
 * @Redirect handler can't call it directly - that's a real javac
 * compile-time access check on the mixin's own source, unrelated to
 * whether Mixin's bytecode transform would technically allow it at
 * runtime. @Invoker is the standard Mixin mechanism for this: it generates
 * a synthetic public bridge method on LevelChunk at transform time that
 * TileEntityTickGateMixin can call normally.
 */
@Mixin(LevelChunk.class)
public interface LevelChunkIsTickingAccessor {
	@Invoker("isTicking")
	boolean turtlePerformer$callIsTicking(BlockPos pos);
}
