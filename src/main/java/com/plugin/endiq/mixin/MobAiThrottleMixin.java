package com.plugin.endiq.mixin;

import com.turtle.performer.entities.DistanceBasedAI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed via genSources against actual MC 26.2 decompiled
 * source: Mob#customServerAiStep(ServerLevel), protected, line 752 of
 * Mob.java. This is the per-mob AI decision step (goal selection,
 * pathfinding decisions) - distinct from Mob#tick()/aiStep(), which still
 * handle physics/movement/damage every tick regardless. Cancelling this
 * specific step means a mob simply doesn't reconsider its AI goals that
 * tick (keeps doing whatever it was already doing), not that it freezes
 * entirely. Default interval=1 in DistanceBasedAI means vanilla-equivalent
 * until configured.
 */
@Mixin(Mob.class)
public class MobAiThrottleMixin {

	public static final DistanceBasedAI THROTTLE = new DistanceBasedAI();
	private static boolean initialized = false;

	@Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
	private void turtlePerformer$onCustomAiStep(ServerLevel level, CallbackInfo ci) {
		if (!initialized) {
			THROTTLE.initialize();
			initialized = true;
		}
		Mob self = (Mob) (Object) this;
		if (!THROTTLE.shouldRunAiStep(level.getGameTime(), self.getId())) {
			ci.cancel();
		}
	}
}
