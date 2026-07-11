package com.plugin.endiq.performance.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Vulkan render backend.
 *
 * On Android/Termux devices Vulkan support depends on the driver stack that is actually
 * loaded (VirGL, Turnip/Freedreno, Zink-over-Vulkan, etc). Rather than assuming Vulkan is
 * available, this backend does a real capability probe:
 *   1. Try to load LWJGL's Vulkan bindings (org.lwjgl.vulkan.VK10). If they're not on the
 *      classpath/native libs, Vulkan is not usable at all.
 *   2. If present, create a throwaway VkInstance and immediately destroy it. If that fails
 *      (missing ICD, no vulkan.so, unsupported device) we report unsupported so the caller
 *      falls back to OpenGL instead of crashing later mid-frame.
 */
public class VulkanBackend implements Backend {
    private static final Logger LOGGER = LoggerFactory.getLogger("performer-turtle-plugin");

    private Boolean cachedSupported;
    private Object vkInstance; // org.lwjgl.vulkan.VkInstance, kept via reflection to avoid a hard dependency

    @Override
    public boolean supported() {
        if (cachedSupported != null) {
            return cachedSupported;
        }
        cachedSupported = probe();
        return cachedSupported;
    }

    private boolean probe() {
        try {
            Class<?> vk10 = Class.forName("org.lwjgl.vulkan.VK10");
            Class<?> vkInstanceCreateInfo = Class.forName("org.lwjgl.vulkan.VkInstanceCreateInfo");
            Class<?> memStackCls = Class.forName("org.lwjgl.system.MemoryStack");

            // Try to actually create + destroy a Vulkan instance. Any failure here means the
            // device/driver combo can't really run Vulkan, even though the Java bindings exist.
            Method stackPush = memStackCls.getMethod("stackPush");
            Object stack = stackPush.invoke(null);
            try {
                Method callocMethod = vkInstanceCreateInfo.getMethod("calloc", memStackCls);
                Object createInfo = callocMethod.invoke(null, stack);
                vkInstanceCreateInfo.getMethod("sType$Default").invoke(createInfo);

                Class<?> pointerBufferCls = Class.forName("org.lwjgl.PointerBuffer");
                Method mallocPointer = memStackCls.getMethod("mallocPointer", int.class);
                Object pInstance = mallocPointer.invoke(stack, 1);

                Method vkCreateInstance = vk10.getMethod("vkCreateInstance", vkInstanceCreateInfo,
                        Class.forName("org.lwjgl.vulkan.VkAllocationCallbacks"), pointerBufferCls);
                int result = (int) vkCreateInstance.invoke(null, createInfo, null, pInstance);

                int vkSuccess = (int) vk10.getField("VK_SUCCESS").get(null);
                if (result != vkSuccess) {
                    LOGGER.info("Vulkan probe failed with VkResult={}, falling back to OpenGL", result);
                    return false;
                }

                long handle = (long) pointerBufferCls.getMethod("get", int.class).invoke(pInstance, 0);
                Class<?> vkInstanceCls = Class.forName("org.lwjgl.vulkan.VkInstance");
                vkInstance = vkInstanceCls.getConstructor(long.class, vkInstanceCreateInfo)
                        .newInstance(handle, createInfo);

                Method destroy = vk10.getMethod("vkDestroyInstance", vkInstanceCls,
                        Class.forName("org.lwjgl.vulkan.VkAllocationCallbacks"));
                destroy.invoke(null, vkInstance, null);
                vkInstance = null;

                return true;
            } finally {
                memStackCls.getMethod("pop").invoke(stack);
            }
        } catch (Throwable t) {
            LOGGER.info("Vulkan not usable on this device ({}), falling back to OpenGL", t.toString());
            return false;
        }
    }

    @Override
    public String name() {
        return "Vulkan";
    }

    @Override
    public void init() {
        // Real instance creation for rendering use happens lazily inside the render pipeline
        // (FastChunkRenderer); probe() above already validated that this is possible.
        LOGGER.info("Vulkan backend initialized");
    }

    @Override
    public void shutdown() {
        vkInstance = null;
    }
}
