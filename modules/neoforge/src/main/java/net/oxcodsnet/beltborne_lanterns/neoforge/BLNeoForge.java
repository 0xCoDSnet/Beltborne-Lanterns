package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.neoforged.fml.common.Mod;

import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;

@Mod(BLMod.MOD_ID)
public final class BLNeoForge {
    public BLNeoForge() {
        LambDynLightsCompat.init();
    }
}
