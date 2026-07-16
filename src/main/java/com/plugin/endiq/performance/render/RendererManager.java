package com.plugin.endiq.performance.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Picks the best available render backend and exposes it to the rest of the mod.
 * Tries Vulkan first; if it isn't actually usable on this device/driver, falls back to OpenGL.
 */
public class RendererManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("performer-turtle-plugin");
    private static RendererManager instance;

    private Backend active;

    public static synchronized RendererManager get() {
        if (instance == null) {
            instance = new RendererManager();
        }
        return instance;
    }

    /** Selects and initializes a backend. Safe to call multiple times (idempotent). */
    public synchronized Backend init() {
        if (active != null) {
            return active;
        }

        Backend vulkan = new VulkanBackend();
        if (vulkan.supported()) {
            active = vulkan;
        } else {
            Backend openGl = new OpenGLBackend();
            if (!openGl.supported()) {
                // This should never happen inside a running Minecraft instance, but don't
                // silently do nothing - force OpenGL anyway since the game already proved
                // it can draw with it.
                LOGGER.warn("OpenGL capability probe failed unexpectedly; using it anyway since Minecraft is already running on it");
            }
            active = openGl;
        }

        active.init();
        LOGGER.info("Renderer backend selected: {}", active.name());
        return active;
    }

    public synchronized Backend active() {
        return active != null ? active : init();
    }

    public synchronized boolean isVulkan() {
        return active() instanceof VulkanBackend;
    }

    public synchronized void shutdown() {
        if (active != null) {
            active.shutdown();
            active = null;
        }
    }
}
