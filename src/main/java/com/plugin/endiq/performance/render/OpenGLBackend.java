package com.plugin.endiq.performance.render;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** OpenGL render backend. Always the safe fallback since Minecraft already runs on it. */
public class OpenGLBackend implements Backend {
    private static final Logger LOGGER = LoggerFactory.getLogger("performer-turtle-plugin");
    private Boolean cachedSupported;

    @Override
    public boolean supported() {
        if (cachedSupported != null) {
            return cachedSupported;
        }
        try {
            GLCapabilities caps = GL.getCapabilities();
            // We need at least GL 3.1 for glDrawElementsInstanced / instanced arrays,
            // which the fast chunk renderer relies on.
            cachedSupported = caps != null && (caps.OpenGL31 || caps.GL_ARB_draw_instanced);
        } catch (Throwable t) {
            // GL context not current on this thread yet - assume yes, Minecraft itself
            // requires OpenGL to run at all, so this is always a safe fallback.
            cachedSupported = true;
        }
        return cachedSupported;
    }

    @Override
    public String name() {
        return "OpenGL";
    }

    @Override
    public void init() {
        LOGGER.info("OpenGL backend initialized");
    }

    @Override
    public void shutdown() {
    }
}
