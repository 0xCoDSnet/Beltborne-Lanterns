package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.neoforged.fml.common.Mod;

import net.oxcodsnet.beltborne_lanterns.BLMod;

@Mod(BLMod.MOD_ID)
public final class BLNeoForge {
    public BLNeoForge() {
        // Run our common setup.
        BLMod.init();
    }
}
