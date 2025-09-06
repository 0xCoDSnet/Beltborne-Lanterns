package net.oxcodsnet.beltborne_lanterns.common.compat;

/**
 * Loader-agnostic bridge to invoke Accessories integration hooks
 * from common code without a compile-time dependency on platform
 * packages.
 */
public final class AccessoriesCompatBridge {
    private AccessoriesCompatBridge() {}

    private static volatile Runnable refreshCallback;

    /**
     * Platform modules should provide a callback that re-registers
     * current LampRegistry items with Accessories.
     */
    public static void setRefreshCallback(Runnable callback) {
        refreshCallback = callback;
    }

    /**
     * Invoke the platform-provided refresh if available.
     */
    public static void refreshRegisteredAccessories() {
        Runnable cb = refreshCallback;
        if (cb != null) {
            try {
                cb.run();
            } catch (Throwable ignored) {
                // No-op if platform integration fails
            }
        }
    }
}

