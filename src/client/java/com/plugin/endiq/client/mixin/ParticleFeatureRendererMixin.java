package com.plugin.endiq.client.mixin;

import com.turtle.performer.particles.ParticleLimiter;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Real mixin target confirmed against Sodium 0.9.1 for MC 26.2
 * (mixin.core.render.world.ParticleFeatureRendererMixin), which injects
 * into QuadParticleFeatureRenderer#executeGroup(FeatureFrameContext, int,
 * List<Submit>, boolean, CallbackInfo). Trims the submit list at the head
 * of the method, before vanilla/Sodium iterates it, using our particle
 * budget for this frame.
 */
@Mixin(QuadParticleFeatureRenderer.class)
public abstract class ParticleFeatureRendererMixin {

	@Inject(method = "executeGroup", at = @At("HEAD"))
	private void turtlePerformer$limitSubmits(FeatureFrameContext context, int groupIndex,
			List<QuadParticleFeatureRenderer.Submit> submits, boolean strictlyOrdered, CallbackInfo ci) {
		ParticleLimiter limiter = GameRendererFrameMixin.PARTICLE_LIMITER;
		if (submits.size() <= limiter.perTypeLimit() * 4) {
			// small group, not worth the list mutation cost
			return;
		}
		int allowed = 0;
		java.util.Iterator<QuadParticleFeatureRenderer.Submit> it = submits.iterator();
		while (it.hasNext()) {
			it.next();
			if (!limiter.tryAllow(groupIndex)) {
				it.remove();
			} else {
				allowed++;
			}
		}
	}
}
