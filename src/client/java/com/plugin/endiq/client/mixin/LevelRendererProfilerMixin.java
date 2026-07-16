package com.plugin.endiq.client.mixin;

import com.turtle.performer.render.RenderProfiler;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real mixin target confirmed against Sodium 0.9.1 for MC 26.2
 * (mixin.core.render.world.LevelRendererMixin), which injects into
 * LevelRenderer#endFrame(CallbackInfo) and
 * LevelRenderer#resetLevelRenderData(CallbackInfo). Both are confirmed
 * present and used as real hook points by Sodium's own client mixins.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererProfilerMixin {

	public static final RenderProfiler PROFILER = new RenderProfiler();

	@Inject(method = "endFrame", at = @At("RETURN"))
	private void turtlePerformer$onEndFrame(CallbackInfo ci) {
		PROFILER.frameEnd();
	}

	@Inject(method = "resetLevelRenderData", at = @At("RETURN"))
	private void turtlePerformer$onTerrainReset(CallbackInfo ci) {
		PROFILER.markTerrainDirty();
	}
}
