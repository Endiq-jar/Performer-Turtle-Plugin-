package com.plugin.endiq.mixin;

import com.turtle.performer.lighting.LightingManager;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Real target confirmed against RelativityMC/ScalableLux (a Starlight fork,
 * actively maintained, branch ver/26.2.0 - exact match for this project's
 * Minecraft version), whose ThreadedLevelLightEngineMixin wraps this exact
 * real vanilla method: net.minecraft.world.level.lighting
 * .LevelLightEngine#runLightUpdates(), which returns the number of light
 * updates processed. ScalableLux redirects the call entirely to swap in a
 * different light engine implementation; that's out of scope for a
 * telemetry-focused mod like this one, so this just records the real
 * per-call update count via @At("RETURN") instead of replacing anything.
 */
@Mixin(LevelLightEngine.class)
public class LightEngineActivityMixin {

	public static final LightingManager MANAGER = new LightingManager();
	private static boolean initialized = false;

	@Inject(method = "runLightUpdates", at = @At("RETURN"))
	private void turtlePerformer$onRunLightUpdates(CallbackInfoReturnable<Integer> cir) {
		if (!initialized) {
			MANAGER.initialize();
			initialized = true;
		}
		MANAGER.recordLightUpdatesProcessed(cir.getReturnValueI());
	}
}
