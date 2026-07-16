package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.FastBlockUpdater;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed against Lithium 0.25.2 for MC 26.2
 * (mixin.entity.inactive_navigations.ServerLevelMixin), which both
 * @Redirect's and @Inject's into this exact method:
 * ServerLevel#sendBlockUpdated(BlockPos, BlockState, BlockState, int).
 * That's vanilla's real per-block-change notification dispatch (client
 * sync + neighbor listeners), confirmed real by a currently-shipping mod
 * hooking the identical signature. Feeds FastBlockUpdater's dedupe/batch
 * queue but does not cancel the vanilla call - see note below.
 */
@Mixin(ServerLevel.class)
public class FastBlockUpdateMixin {

	public static final FastBlockUpdater UPDATER = new FastBlockUpdater();
	private static boolean initialized = false;

	@Inject(method = "sendBlockUpdated", at = @At("HEAD"))
	private void turtlePerformer$onBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState,
			int flags, CallbackInfo ci) {
		if (!initialized) {
			UPDATER.initialize();
			initialized = true;
		}
		UPDATER.queueUpdate(pos.asLong());
		// Not cancelled: this notification drives client sync, so skipping
		// it outright would desync the client's view of the block. This
		// hook is for dedupe/batching accounting only, not suppression.
	}
}
