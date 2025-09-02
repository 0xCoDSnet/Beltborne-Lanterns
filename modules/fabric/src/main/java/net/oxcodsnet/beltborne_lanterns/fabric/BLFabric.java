package net.oxcodsnet.beltborne_lanterns.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.LampConfigSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;

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

        // LambDynamicLights integration is client-only; initialized from BLFabricClient.

        // Register payload types for networking.
        // These are common and need to be registered on both client and server.
        PayloadTypeRegistry.playS2C().register(BeltSyncPayload.ID, BeltSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LampConfigSyncPayload.ID, LampConfigSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleLanternPayload.ID, ToggleLanternPayload.CODEC);

        // Register all server-side events
        BLFabricServerEvents.initialize();
        BLMod.LOGGER.debug("Registered networking and server event handlers [Fabric]");
    }
}
