package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.common.server.BeltLanternServer;

/**
 * Server-side interaction + sync logic for NeoForge.
 */
@EventBusSubscriber(modid = BLMod.MOD_ID)
public final class BLNeoForgeServerEvents {
    private BLNeoForgeServerEvents() {}


    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent e) {
        LampRegistry.init();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayerEntity joining)) return;
        MinecraftServer server = joining.server;
        // Restore from persistent save and broadcast
        Item persisted = BeltLanternSave.get(server).get(joining.getUuid());
        BeltState.setLamp(joining, persisted);
        BeltNetworking.broadcastBeltState(joining, persisted);
        for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
            Item lamp = BeltState.getLamp(other);
            BeltNetworking.sendTo(joining, other.getUuid(), lamp);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayerEntity leaving)) return;
        Item lamp = BeltState.getLamp(leaving);
        BeltLanternSave.get(leaving.server).set(leaving.getUuid(), lamp);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayerEntity)) return;
        if (!event.isWasDeath()) return;
        ServerPlayerEntity oldPlayer = (ServerPlayerEntity) event.getOriginal();
        boolean keep = oldPlayer.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
        BeltLanternServer.handleDeath(oldPlayer, keep);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayerEntity player)) return;
        Item lamp = BeltState.getLamp(player);
        BeltNetworking.broadcastBeltState(player, lamp);
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        LampRegistry.init();
    }
}
