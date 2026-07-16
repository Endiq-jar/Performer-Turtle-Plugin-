package com.plugin.endiq.performance.render;

/** A renderer backend that the mod can drive. */
public interface Backend {
    /** Whether this backend can actually be initialized on the current system. */
    boolean supported();

    /** Human readable name, used for logs/HUD. */
    String name();

    /** Perform any one-time setup. Called once, after {@link #supported()} returned true. */
    void init();

    /** Release any native resources held by this backend. */
    void shutdown();
}
