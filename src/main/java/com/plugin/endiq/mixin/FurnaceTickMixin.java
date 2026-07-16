package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.FurnaceOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target and real shadowed fields confirmed against Lithium 0.25.2 for
 * MC 26.2 (mixin.world.block_entity_ticking.sleeping.furnace
 * .AbstractFurnaceBlockEntityMixin), which shadows exactly
 * `public int cookingTimer;` and `public int litTimeRemaining;` on this
 * same class, and hooks the same static
 * AbstractFurnaceBlockEntity#serverTick(ServerLevel, BlockPos, BlockState,
 * AbstractFurnaceBlockEntity, CallbackInfo) entry point. Lithium's real
 * safety condition for "nothing will change if we skip" is exactly
 * `!isLit() && cookingTimer == 0`; we reuse that same confirmed condition
 * to decide when to record idle state via FurnaceOptimizer.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceTickMixin {

	public static final FurnaceOptimizer OPTIMIZER = new FurnaceOptimizer();
	private static boolean initialized = false;

	@SuppressWarnings("ShadowModifiers")
	@Shadow
	public int cookingTimer;

	@SuppressWarnings("ShadowModifiers")
	@Shadow
	public int litTimeRemaining;

	@Inject(method = "serverTick", at = @At("RETURN"))
	private static void turtlePerformer$afterServerTick(ServerLevel level, BlockPos pos, BlockState state,
			AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		FurnaceTickMixin self = (FurnaceTickMixin) (Object) blockEntity;
		boolean isBurning = self.litTimeRemaining > 0;
		boolean isCooking = self.cookingTimer > 0;
		// Same confirmed-safe idle condition Lithium itself relies on before
		// letting this furnace go to sleep: nothing changes on an unlit,
		// non-cooking furnace unless a neighboring inventory update wakes it.
		OPTIMIZER.shouldTick(pos.asLong(), isBurning, isCooking, isBurning);
	}
}
