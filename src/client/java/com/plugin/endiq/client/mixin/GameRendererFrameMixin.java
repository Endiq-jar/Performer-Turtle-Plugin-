package com.plugin.endiq.client.mixin;

import com.turtle.performer.adaptive.AdaptiveParticleManager;
import com.turtle.performer.adaptive.AdaptiveQualityManager;
import com.turtle.performer.culling.ParticleCuller;
import com.turtle.performer.particles.ParticleLimiter;
import com.turtle.performer.render.DynamicRenderDistance;
import com.turtle.performer.util.FpsCounter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real mixin target confirmed against Sodium 0.9.1 for MC 26.2
 * (mixin.features.gui.hooks.console.GameRendererMixin), which injects
 * into GameRenderer#render(DeltaTracker, boolean, CallbackInfo).
 * Used here as the once-per-frame hook to drive adaptive quality,
 * adaptive particle scaling, and the per-frame particle budget reset.
 */
@Mixin(GameRenderer.class)
public class GameRendererFrameMixin {

	public static final FpsCounter FPS_COUNTER = new FpsCounter();
	public static final AdaptiveQualityManager QUALITY_MANAGER = new AdaptiveQualityManager();
	public static final AdaptiveParticleManager PARTICLE_MANAGER = new AdaptiveParticleManager();
	public static final ParticleLimiter PARTICLE_LIMITER = new ParticleLimiter();
	public static final DynamicRenderDistance RENDER_DISTANCE = new DynamicRenderDistance();

	private static boolean initialized = false;
	private static int lastKnownFps = 60;

	@Inject(method = "render", at = @At("HEAD"))
	private void turtlePerformer$onFrameStart(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		if (!initialized) {
			QUALITY_MANAGER.initialize();
			PARTICLE_MANAGER.initialize();
			PARTICLE_LIMITER.initialize();
			ParticleSpawnMixin.CULLER.initialize();
			RENDER_DISTANCE.initialize();
			initialized = true;
		}

		int measured = FPS_COUNTER.frame();
		if (measured >= 0) {
			lastKnownFps = measured;
		}

		QUALITY_MANAGER.update(lastKnownFps);
		PARTICLE_MANAGER.update(lastKnownFps, 60.0);
		PARTICLE_LIMITER.resetTick();
		ParticleSpawnMixin.CULLER.resetTick();

		// PARTICLE_MANAGER.scale() was previously computed every frame and
		// never read anywhere - it had no effect on the game. Apply it to
		// the particle-spawn culler (which does actually cancel spawns) so
		// low FPS genuinely reduces particle volume instead of just
		// tracking a number nobody used.
		int baseParticleLimit = 64;
		int scaledLimit = Math.max(1, (int) Math.round(baseParticleLimit * PARTICLE_MANAGER.scale()));
		ParticleSpawnMixin.CULLER.setPerTypeLimit(scaledLimit);

		TextureAtlasActivityMixin.OPTIMIZER.resetTick();
		BakedGlyphActivityMixin.CACHE.resetFrame();

		// Real accessor confirmed against current Sodium source
		// (client/gui/SodiumConfigBuilder.java binds its own render-distance
		// slider to this exact call: vanillaOpts.renderDistance().set(...)/
		// .get()) - net.minecraft.client.Options#renderDistance() returns
		// the real OptionInstance<Integer> vanilla itself uses for the
		// video-settings slider. AdaptiveQualityManager's tier was computed
		// every frame and never used for anything until now - this makes
		// low measured FPS actually pull render distance down (and restore
		// it once FPS recovers), rate-limited inside DynamicRenderDistance
		// so it doesn't thrash chunk loading tickets every frame.
		var options = Minecraft.getInstance().options;
		RENDER_DISTANCE.captureBaseline(options.renderDistance().get());
		int proposed = RENDER_DISTANCE.proposeDistance(QUALITY_MANAGER.getQualityTier(), System.nanoTime());
		if (proposed > 0) {
			options.renderDistance().set(proposed);
		}
	}
}
