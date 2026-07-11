package com.plugin.endiq.performance.config;

public class PerformanceConfig {
    public boolean dynamicRenderDistance = true;
    public boolean entityCulling = true;
    public boolean greedyMeshing = true;
    public boolean asyncChunkUpload = true;

    // New fast chunk rendering pipeline toggles
    public boolean fastChunkRenderer = true;
    public boolean vulkanBackend = true;      // if false, always force OpenGL regardless of Vulkan support
    public boolean meshCompression = true;
    public boolean vertexDeduplication = true;
    public boolean vertexBufferOptimization = true;
    public boolean gpuInstancing = true;
    public boolean multiDrawIndirect = true;
}
