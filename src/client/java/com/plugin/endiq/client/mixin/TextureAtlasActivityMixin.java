package com.plugin.endiq.client.mixin;

import com.turtle.performer.resources.TextureRebuildOptimizer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Real target confirmed against current CaffeineMC/sodium source
 * (mixin.features.textures.animations.tracking.TextureAtlasMixin), which
 * injects into this exact same method - TextureAtlas#getSprite(...),
 * @At("RETURN") - to mark sprites active for its own animation-ticking
 * optimization. Left unqualified (no descriptor) same as Sodium's own
 * mixin, relying on the method name being unambiguous on this class.
 * Record-only: feeds TextureRebuildOptimizer real per-tick sprite access
 * data, does not alter atlas/sprite behavior.
 */
@Mixin(TextureAtlas.class)
public class TextureAtlasActivityMixin {

	public static final TextureRebuildOptimizer OPTIMIZER = new TextureRebuildOptimizer();
	private static boolean initialized = false;

	@Inject(method = "getSprite", at = @At("RETURN"))
	private void turtlePerformer$onGetSprite(CallbackInfoReturnable<TextureAtlasSprite> cir) {
		if (!initialized) {
			OPTIMIZER.initialize();
			initialized = true;
		}
		TextureAtlasSprite sprite = cir.getReturnValue();
		if (sprite != null) {
			OPTIMIZER.recordSpriteAccessed(sprite.contents().name().toString());
		}
	}
}
