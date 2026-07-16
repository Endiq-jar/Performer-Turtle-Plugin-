package com.plugin.endiq.client.mixin;

import com.turtle.performer.memory.FontCache;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real target confirmed against current CaffeineMC/sodium source
 * (mixin.features.render.gui.font.BakedGlyphMixin), which injects into this
 * exact real vanilla class - net.minecraft.client.gui.font.glyphs
 * .BakedSheetGlyph#render(...). Sodium's version cancels and replaces the
 * vertex writing (needs vertex-format internals this project hasn't
 * independently confirmed), so this only records that a real glyph render
 * happened, feeding memory.FontCache's per-frame counter instead of
 * cancelling anything.
 */
@Mixin(BakedSheetGlyph.class)
public class BakedGlyphActivityMixin {

	public static final FontCache CACHE = new FontCache();
	private static boolean initialized = false;

	@Inject(method = "render", at = @At("HEAD"))
	private void turtlePerformer$onRender(CallbackInfo ci) {
		if (!initialized) {
			CACHE.initialize();
			initialized = true;
		}
		CACHE.recordGlyphRender();
	}
}
