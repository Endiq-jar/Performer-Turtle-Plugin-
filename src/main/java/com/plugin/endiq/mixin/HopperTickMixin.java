package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.HopperOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target and real shadowed fields confirmed against Lithium 0.25.2 for
 * MC 26.2 (mixin.block.hopper.HopperBlockEntityMixin), which shadows exactly
 * `isOnCooldown()`, `setCooldown(int)` and `tickedGameTime` on this same
 * class. Static method HopperBlockEntity#pushItemsTick(Level, BlockPos,
 * BlockState, HopperBlockEntity, CallbackInfo) is the confirmed real tick
 * entry point. Unlike Lithium's full sleep/wake system (which requires
 * inventory-change listeners and entity-movement trackers we don't have),
 * this layers a second, more aggressive cooldown on top of vanilla's own
 * using HopperOptimizer, and actually cancels the tick (skipping
 * tryMoveItems entirely, including its container lookups) when throttled.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperTickMixin {

	public static final HopperOptimizer OPTIMIZER = new HopperOptimizer();
	private static boolean initialized = false;

	@Shadow
	protected abstract boolean isOnCooldown();

	@Shadow
	protected abstract void setCooldown(int cooldown);

	@Inject(method = "pushItemsTick", at = @At("HEAD"), cancellable = true)
	private static void turtlePerformer$throttle(Level level, BlockPos pos, BlockState state,
			HopperBlockEntity blockEntity, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}

		HopperTickMixin self = (HopperTickMixin) (Object) blockEntity;
		if (self.isOnCooldown()) {
			// vanilla's own cooldown already governs this tick, nothing to add
			return;
		}

		long posKey = pos.asLong();
		if (!OPTIMIZER.tryTransfer(posKey, level.getGameTime())) {
			ci.cancel();
			return;
		}
	}

	@Inject(method = "pushItemsTick", at = @At("RETURN"))
	private static void turtlePerformer$recordResult(Level level, BlockPos pos, BlockState state,
			HopperBlockEntity blockEntity, CallbackInfo ci) {
		if (!initialized) {
			return;
		}
		HopperTickMixin self = (HopperTickMixin) (Object) blockEntity;
		long posKey = pos.asLong();
		// We don't have direct visibility into whether items actually moved
		// from a plain HEAD/RETURN pair (that lives inside tryMoveItems'
		// return value, which this method doesn't expose). Approximate using
		// vanilla's own post-tick cooldown state as the signal instead:
		// vanilla only sets its cooldown after a successful transfer, so
		// "still not on cooldown" is our best available proxy for "nothing
		// moved" (empty source or full destination). Previously this passed
		// sourceEmpty/destFull as hardcoded false, which made the aggressive
		// emptyBackoff branch in HopperOptimizer unreachable - fixed here.
		boolean stillOnCooldown = self.isOnCooldown();
		boolean approxIdle = !stillOnCooldown;
		OPTIMIZER.recordResult(posKey, level.getGameTime(), stillOnCooldown, approxIdle, approxIdle);
	}
}
