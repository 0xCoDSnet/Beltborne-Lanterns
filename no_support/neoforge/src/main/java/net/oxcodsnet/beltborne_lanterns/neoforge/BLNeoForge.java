package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;

import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayerRegistry;

@Mod(BLMod.MOD_ID)
public final class BLNeoForge {
    public BLNeoForge() {
        String version = ModList.get()
                .getModContainerById(BLMod.MOD_ID)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("?");
        BLMod.LOGGER.info("Initializing v{} [NeoForge]", version);

        // Load all compatibility layers
        CompatibilityLayerRegistry.loadLayers(ModList.get()::isLoaded);

        // Initialize all loaded compatibility layers
        CompatibilityLayerRegistry.initializeLayers();
    }
}
