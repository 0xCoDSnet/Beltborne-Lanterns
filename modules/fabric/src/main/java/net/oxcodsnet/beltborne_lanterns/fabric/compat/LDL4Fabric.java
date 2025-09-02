package net.oxcodsnet.beltborne_lanterns.fabric.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;

/**
 * Legacy shim kept for older init flow; real 4.x integration uses API entrypoint.
 */
public final class LDL4Fabric {
    private static boolean tried = false;

    private LDL4Fabric() {}

    public static boolean tryInit() {
        if (LambDynLightsCompat.isInitialized()) return true;
        if (tried) return false;
        tried = true;
        // With LDL4 API, registration happens via DynamicLightsInitializer entrypoint.
        // Here we only hint availability; actual init will markInitialized() when called by LDL.
        return FabricLoader.getInstance().isModLoaded("lambdynlights");
    }
}
