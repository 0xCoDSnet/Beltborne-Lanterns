package net.oxcodsnet.beltborne_lanterns;

import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;

public final class BLMod {
    public static final String MOD_ID = "beltborne_lanterns";

    public static void init() {
        LampRegistry.init();
    }
}
