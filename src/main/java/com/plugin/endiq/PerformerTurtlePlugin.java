package com.plugin.endiq;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turtle.performer.compatibility.CompatibilityManager;
import com.plugin.endiq.config.ModConfig;

import com.plugin.endiq.mixin.RandomTickBlockMixin;
import com.plugin.endiq.mixin.FluidTickMixin;
import com.plugin.endiq.mixin.MobAiThrottleMixin;
import com.plugin.endiq.mixin.ObserverUpdateMixin;
import com.plugin.endiq.mixin.HopperTickMixin;
import com.plugin.endiq.mixin.CropRandomTickMixin;
import com.plugin.endiq.mixin.TileEntityTickGateMixin;

public class PerformerTurtlePlugin implements ModInitializer {
	public static final String MOD_ID = "performer-turtle-plugin";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final CompatibilityManager COMPATIBILITY = new CompatibilityManager();
	public static ModConfig CONFIG;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		COMPATIBILITY.initialize();

		// Every interval below used to be hardcoded here - now it's loaded
		// from config/turtle-performer.json (created with these same
		// values on first run), so tuning any of it no longer needs a
		// recompile. See ModConfig for the file format.
		CONFIG = ModConfig.loadOrCreate();
		RandomTickBlockMixin.OPTIMIZER.setInterval(CONFIG.randomTickBlockInterval);
		FluidTickMixin.OPTIMIZER.setInterval(CONFIG.fluidTickInterval);
		MobAiThrottleMixin.THROTTLE.setInterval(CONFIG.mobAiThrottleInterval);
		ObserverUpdateMixin.OPTIMIZER.setMinPulseInterval(CONFIG.observerMinPulseInterval);
		HopperTickMixin.OPTIMIZER.setEmptyBackoff(CONFIG.hopperEmptyBackoff);
		CropRandomTickMixin.OPTIMIZER.setMatureSampleRate(CONFIG.cropMatureSampleRate);
		TileEntityTickGateMixin.aggressiveModeEnabled = CONFIG.tileEntityAggressiveGate;

		String mcVersion = FabricLoader.getInstance()
			.getModContainer("minecraft")
			.map(c -> c.getMetadata().getVersion().getFriendlyString())
			.orElse("unknown");
		COMPATIBILITY.versionSupport().setCurrentVersion(mcVersion);

		boolean dedicated = FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER;
		COMPATIBILITY.server().setEnvironment(dedicated, true);

		FabricLoader.getInstance().getAllMods().forEach(mod ->
			COMPATIBILITY.modLayer().registerLoadedMod(mod.getMetadata().getId()));
		COMPATIBILITY.modLayer().resolveConflicts();

		if (!COMPATIBILITY.versionSupport().isSupported(mcVersion)) {
			LOGGER.warn("TurtlePerformer: unrecognized Minecraft version {}, running with default settings", mcVersion);
		} else {
			LOGGER.info("TurtlePerformer: compatible with Minecraft {} ({})", mcVersion,
				COMPATIBILITY.versionSupport().jreTierFor(mcVersion));
		}
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
