package com.plugin.endiq.client.mixin;

import com.turtle.performer.culling.ParticleCuller;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: ClientLevel#addParticle(ParticleOptions, double, double, double,
 * double, double, double), public, line 877 of ClientLevel.java. This is
 * the real particle-spawn entry point (earlier stage than
 * ParticleFeatureRendererMixin's render-time submit-list trimming from a
 * previous session - cancelling here skips object allocation entirely,
 * not just the render call). Cancellation is safe: a particle that never
 * spawns has no other observable game-state effect. Default
 * perTypeLimit=64 in ParticleCuller is generous, not aggressive.
 */
@Mixin(ClientLevel.class)
public class ParticleSpawnMixin {

	public static final ParticleCuller CULLER = new ParticleCuller();
	private static boolean initialized = false;

	@Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void turtlePerformer$onAddParticle(ParticleOptions particle, double x, double y, double z,
			double xd, double yd, double zd, CallbackInfo ci) {
		if (!initialized) {
			CULLER.initialize();
			initialized = true;
		}
		String typeKey = particle.getType().toString();
		if (!CULLER.trySpawn(typeKey)) {
			ci.cancel();
		}
	}
}
