package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.TileEntityTickOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Real target confirmed against Lithium 0.25.2 for MC 26.2
 * (mixin.world.block_entity_ticking.world_border.DirectBlockEntityTickInvokerMixin),
 * which redirects this exact same call:
 * LevelChunk$BoundTickingBlockEntity#tick() -> LevelChunk#isTicking(BlockPos).
 * Real shadowed fields `this$0` (the owning LevelChunk) and `getPos()` are
 * confirmed by that same mixin. We don't have a confirmed real signature for
 * a nearest-player-distance API in either reference source, so this throttles
 * by idle-tick sampling only (real skip, no distance component) rather than
 * guessing a method name that could silently fail to compile or match.
 */
@Mixin(targets = "net/minecraft/world/level/chunk/LevelChunk$BoundTickingBlockEntity")
public abstract class TileEntityTickGateMixin {

	public static final TileEntityTickOptimizer OPTIMIZER = new TileEntityTickOptimizer();
	private static boolean initialized = false;

	/**
	 * OFF by default and deliberately so: this redirect can't tell an idle
	 * block entity from a busy one (no type-specific state available here),
	 * so enabling it throttles EVERY ticking block entity uniformly - a
	 * furnace mid-smelt, a brewing stand, a hopper mid-transfer all get
	 * skipped 3 out of every 4 ticks once turtlePerformer$idleTicks passes
	 * the threshold. That's a real, silent gameplay-timing change, not a
	 * free optimization. Flip this only if you've accepted that tradeoff.
	 */
	public static boolean aggressiveModeEnabled = false;

	@Shadow
	@Final
	LevelChunk this$0;

	@Shadow
	public abstract BlockPos getPos();

	private int turtlePerformer$idleTicks = 0;

	@Redirect(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/chunk/LevelChunk;isTicking(Lnet/minecraft/core/BlockPos;)Z"
			)
	)
	private boolean turtlePerformer$gatedIsTicking(LevelChunk instance, BlockPos pos) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		boolean vanillaAllowsTick = instance.isTicking(pos);
		if (!vanillaAllowsTick || !aggressiveModeEnabled) {
			return vanillaAllowsTick;
		}
		turtlePerformer$idleTicks++;
		// distSqToNearestPlayer left at 0.0 (treated as always-near) since we
		// don't have a confirmed real distance API here; this only applies
		// the idle-tick sampling half of TileEntityTickOptimizer, not the
		// distance cutoff half.
		return OPTIMIZER.shouldTick(turtlePerformer$idleTicks, 0.0);
	}
}
