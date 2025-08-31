package net.oxcodsnet.beltborne_lanterns.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;


import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.common.server.BeltLanternServer;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;
// no direct config init here; client handles config lazily

public final class BLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        BLMod.init();
        LambDynLightsCompat.init();

        // Config is initialized lazily on the client when accessed.

        // Register payload types for networking
        PayloadTypeRegistry.playS2C().register(BeltSyncPayload.ID, BeltSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleLanternPayload.ID, ToggleLanternPayload.CODEC);

        // Handle client toggle requests
        ServerPlayNetworking.registerGlobalReceiver(ToggleLanternPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                ItemStack stack = player.getMainHandStack();
                boolean hasLantern = BeltState.hasLantern(player);
                if (!hasLantern && !stack.isOf(Items.LANTERN)) {
                    stack = player.getOffHandStack();
                    if (!stack.isOf(Items.LANTERN)) return;
                }
                boolean nowHas = BeltLanternServer.toggleLantern(player, stack);
                BeltNetworking.broadcastBeltState(player, nowHas);
            });
        });

        // When a player joins, sync known belt states of all players to them and restore theirs
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joining = handler.getPlayer();
            // Restore from persistent save
            boolean persisted = BeltLanternSave.get(server).has(joining.getUuid());
            BeltState.setHasLantern(joining, persisted);
            // Tell everyone (and self) about joining player's state
            BeltNetworking.broadcastBeltState(joining, persisted);
            // Send existing players' states to the joining player
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                boolean has = BeltState.hasLantern(other);
                BeltNetworking.sendTo(joining, other.getUuid(), has);
            }
        });

        // On disconnect, persist the current state for that player
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity leaving = handler.getPlayer();
            boolean has = BeltState.hasLantern(leaving);
            BeltLanternSave.get(server).set(leaving.getUuid(), has);
        });
    }

    // no-op methods removed: toggling now handled via network payload
}
