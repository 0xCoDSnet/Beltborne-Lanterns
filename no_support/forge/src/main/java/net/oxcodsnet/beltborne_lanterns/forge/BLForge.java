package net.oxcodsnet.beltborne_lanterns.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.oxcodsnet.beltborne_lanterns.BLMod;

@Mod(BLMod.MOD_ID)
public final class BLForge {
    public BLForge() {
        EventBuses.registerModEventBus(BLMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        BLMod.init();
    }
}
