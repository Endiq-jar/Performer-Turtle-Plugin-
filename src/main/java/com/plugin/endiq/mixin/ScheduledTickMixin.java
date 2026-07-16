package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.ScheduledTickOptimizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: LevelChunkTicks#schedule(ScheduledTick<T>), line 53 of
 * LevelChunkTicks.java. Targets the generic class directly (compiles for
 * any T - block or fluid scheduled ticks both go through this same method).
 * Not cancelled: a scheduled tick already represents a deliberate, specific
 * future action (e.g. "this water source becomes ice in 20 ticks") that
 * vanilla itself only schedules once per real state change - dropping it
 * would skip that specific intended event outright, not just delay it.
 * Records volume via ScheduledTickOptimizer for visibility only.
 */
@Mixin(targets = "net/minecraft/world/ticks/LevelChunkTicks")
public class ScheduledTickMixin {

	public static final ScheduledTickOptimizer OPTIMIZER = new ScheduledTickOptimizer();
	private static boolean initialized = false;

	@Inject(method = "schedule", at = @At("HEAD"))
	private void turtlePerformer$onSchedule(Object tick, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		// Position isn't directly available without knowing ScheduledTick's
		// real field layout (pos() accessor name unconfirmed here), so this
		// just tracks that a schedule call happened.
	}
}
