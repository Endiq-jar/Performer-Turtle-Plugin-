package com.plugin.endiq.mixin;

import com.turtle.performer.ticks.PistonOptimizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Real target confirmed against Lithium 0.25.2 for MC 26.2
 * (mixin.alloc.enum_values.piston_handler.PistonStructureResolverMixin),
 * which hooks PistonStructureResolver#addBranchingBlocks(BlockPos) -> boolean.
 * Confirmed exact signature/class from a currently-shipping mod.
 */
@Mixin(PistonStructureResolver.class)
public class PistonStructureMixin {

	public static final PistonOptimizer OPTIMIZER = new PistonOptimizer();
	private static boolean initialized = false;

	@Inject(method = "addBranchingBlocks", at = @At("HEAD"))
	private void turtlePerformer$onBranchCheck(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		OPTIMIZER.tryStartMove(pos.asLong());
	}
}
