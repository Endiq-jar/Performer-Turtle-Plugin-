package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.DynamicTickRate;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: ServerLevel#tick(BooleanSupplier), line 355 of ServerLevel.java -
 * the real per-level, per-tick entry point.
 *
 * genSources also confirmed real classes TickRateManager/ServerTickRateManager
 * with setTickRate(float)/tickrate() - the actual backing for the vanilla
 * /tick command. However, there is no confirmed real accessor for obtaining
 * a ServerTickRateManager instance from within ServerLevel in either
 * genSources output or the Lithium/Sodium reference sources (no
 * "tickRateManager()" or similar call appears anywhere in either). Guessing
 * that accessor chain (e.g. level.getServer().tickRateManager()) risks a
 * mixin that fails to compile or silently targets the wrong thing, so this
 * mixin only feeds DynamicTickRate real per-tick timing data - it does not
 * push the result into the real tick-rate API. That link needs one more
 * confirmed signature before it can happen.
 *
 * Also used as a convenient, already-real periodic hook to bound three
 * data structures elsewhere in this mod that otherwise grow forever with
 * no eviction (FastBlockUpdater's queue, PistonOptimizer's moving set,
 * BlockEventOptimizer's lastEvent map) - they were being fed real data via
 * confirmed real injection points but nothing ever drained them, so they
 * were a slow real memory leak for the life of the world. This doesn't
 * require any new, unconfirmed Minecraft target; it reuses this one.
 */
@Mixin(ServerLevel.class)
public class DynamicTickRateMixin {

	public static final DynamicTickRate MANAGER = new DynamicTickRate();
	private static boolean initialized = false;
	private static long lastTickNanos = System.nanoTime();
	private static int tickCounter = 0;

	// Chosen generously (5s at 20 TPS) so this is a safety net against
	// unbounded growth, not a replacement for a real per-event drain point.
	private static final int CLEANUP_INTERVAL_TICKS = 100;

	@Inject(method = "tick", at = @At("HEAD"))
	private void turtlePerformer$onTick(BooleanSupplier haveTime, CallbackInfo ci) {
		if (!initialized) {
			MANAGER.initialize();
			initialized = true;
		}
		long now = System.nanoTime();
		long deltaNanos = now - lastTickNanos;
		lastTickNanos = now;
		double measuredTps = deltaNanos > 0 ? Math.min(20.0, 1_000_000_000.0 / deltaNanos) : 20.0;
		MANAGER.update(measuredTps, 0);
		// MANAGER.currentTickMs() now reflects real measured server load;
		// wiring it into ServerTickRateManager.setTickRate(...) is the
		// remaining step once that accessor is confirmed.

		if (++tickCounter >= CLEANUP_INTERVAL_TICKS) {
			tickCounter = 0;
			// FastBlockUpdater: actually drain the queue that
			// FastBlockUpdateMixin has been filling since world load with
			// nothing ever reading it.
			java.util.List<Long> drained = FastBlockUpdateMixin.UPDATER.drainBatch();
			FastBlockUpdateMixin.UPDATER.releaseBatch(drained);
			// PistonOptimizer: finishMove() was never called anywhere
			// (no confirmed real "piston move finished" hook), so
			// tryStartMove() entries accumulated forever. Bound it by
			// periodic full clear rather than leaving it unbounded.
			PistonStructureMixin.OPTIMIZER.clearAll();
			// BlockEventOptimizer: same shape of leak - dedupe map keyed
			// by every distinct block-event position ever seen, never
			// pruned.
			BlockEventMixin.OPTIMIZER.clearAll();
		}
	}
}

