package net.oxcodsnet.beltborne_lanterns.neoforge.compat;

import net.neoforged.fml.ModList;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;

/**
 * Legacy shim kept for older init flow; real 4.x integration uses API entrypoint.
 */
public final class LDL4NeoForge {
    private static boolean tried = false;

    private LDL4NeoForge() {}

    public static boolean tryInit() {
        if (LambDynLightsCompat.isInitialized()) return true;
        if (tried) return false;
        tried = true;
        // On NeoForge, LDL4 will discover our initializer via ServiceLoader.
        return ModList.get().isLoaded("lambdynlights");
    }
}
