package net.oxcodsnet.beltborne_lanterns.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayerRegistry;
public final class BLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Basic startup banner (helps players confirm mod loaded)
        String version = FabricLoader.getInstance()
                .getModContainer(BLMod.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");
        BLMod.LOGGER.info("Initializing v{} [Fabric]", version);

        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Load all compatibility layers
        CompatibilityLayerRegistry.loadLayers(FabricLoader.getInstance()::isModLoaded);

        // Initialize all loaded compatibility layers
        CompatibilityLayerRegistry.initializeLayers();

        // Register all server-side events
        BLFabricServerEvents.initialize();
        BLMod.LOGGER.debug("Registered networking and server event handlers [Fabric]");
    }
}
