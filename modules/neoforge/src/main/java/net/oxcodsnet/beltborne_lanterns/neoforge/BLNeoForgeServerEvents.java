package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;

/**
 * Server-side interaction + sync logic for NeoForge.
 */
@EventBusSubscriber(modid = BLMod.MOD_ID)
public final class BLNeoForgeServerEvents {
    private BLNeoForgeServerEvents() {}


    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayerEntity joining)) return;
        MinecraftServer server = joining.server;
        // Restore from persistent save and broadcast
        boolean persisted = BeltLanternSave.get(server).has(joining.getUuid());
        BeltState.setHasLantern(joining, persisted);
        BeltNetworking.broadcastBeltState(joining, persisted);
        for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
            boolean has = BeltState.hasLantern(other);
            BeltNetworking.sendTo(joining, other.getUuid(), has);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayerEntity leaving)) return;
        boolean has = BeltState.hasLantern(leaving);
        BeltLanternSave.get(leaving.server).set(leaving.getUuid(), has);
    }

}
