package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;

/**
 * Registers network payloads on the MOD bus for the dedicated server side.
 */
@EventBusSubscriber(modid = BLMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public final class BLNeoForgeNetwork {
    private BLNeoForgeNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // Server registers clientbound payload with a no-op handler, so it can encode
        var registrar = event.registrar("1");
        registrar.playToClient(BeltSyncPayload.ID, BeltSyncPayload.CODEC, (payload, ctx) -> { /* no-op on server */ });
    }
}
