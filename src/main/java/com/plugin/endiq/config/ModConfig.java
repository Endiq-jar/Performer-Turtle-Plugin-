package com.plugin.endiq.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Every throttle interval in this mod was previously hardcoded in
 * PerformerTurtlePlugin#onInitialize() - no way to tune per-world without
 * recompiling. This loads/saves a plain JSON file under the real Fabric
 * config directory (FabricLoader#getConfigDir() - real, standard Fabric
 * API) so all of it is user-tunable without touching source.
 *
 * Uses Gson, which is bundled with vanilla Minecraft itself (vanilla's
 * own data/resource loading depends on it), so this adds no new
 * dependency risk.
 */
public class ModConfig {

	private static final String FILE_NAME = "turtle-performer.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public int randomTickBlockInterval = 4;
	public int fluidTickInterval = 3;
	public int mobAiThrottleInterval = 3;
	public int observerMinPulseInterval = 3;
	public int hopperEmptyBackoff = 20;
	public int cropMatureSampleRate = 8;
	public boolean tileEntityAggressiveGate = true;

	public static ModConfig loadOrCreate() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
				if (loaded != null) {
					return loaded;
				}
			} catch (IOException | com.google.gson.JsonSyntaxException e) {
				// Fall through to defaults below; a broken/missing config
				// file should never prevent the mod from loading.
			}
		}
		ModConfig defaults = new ModConfig();
		defaults.save();
		return defaults;
	}

	public void save() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			// Non-fatal: worst case the mod just keeps running with
			// whatever values are already in memory.
		}
	}
}
