package net.oxcodsnet.beltborne_lanterns.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;


import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.config.BLLampConfigAccess;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.LampConfigSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.common.server.BeltLanternServer;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;

import java.util.LinkedHashMap;
// no direct config init here; client handles config lazily

public final class BLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        //BLMod.init();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> LampRegistry.init());
        LambDynLightsCompat.init();

        // Config is initialized lazily on the client when accessed.

        // Register payload types for networking
        PayloadTypeRegistry.playS2C().register(BeltSyncPayload.ID, BeltSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LampConfigSyncPayload.ID, LampConfigSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleLanternPayload.ID, ToggleLanternPayload.CODEC);

        // Handle client toggle requests
        ServerPlayNetworking.registerGlobalReceiver(ToggleLanternPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                ItemStack stack = player.getMainHandStack();
                boolean hasLamp = BeltState.hasLamp(player);
                if (!hasLamp && !LampRegistry.isLamp(stack)) {
                    stack = player.getOffHandStack();
                    if (!LampRegistry.isLamp(stack)) return;
                }
                Item nowHas = BeltLanternServer.toggleLantern(player, stack);
                BeltNetworking.broadcastBeltState(player, nowHas);
            });
        });

        // When a player joins, sync known belt states of all players to them and restore theirs
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joining = handler.getPlayer();
            // Restore from persistent save
            Item persisted = BeltLanternSave.get(server).get(joining.getUuid());
            BeltState.setLamp(joining, persisted);
            // Tell everyone (and self) about joining player's state
            BeltNetworking.broadcastBeltState(joining, persisted);
            // Send lamp config from server to joining player
            var lampMap = new LinkedHashMap<Identifier, Integer>();
            BLLampConfigAccess.get().extraLampLight.forEach((idStr, lum) -> {
                Identifier id = Identifier.tryParse(idStr);
                if (id != null) lampMap.put(id, lum);
            });
            ServerPlayNetworking.send(joining, new LampConfigSyncPayload(lampMap));
            // Send existing players' states to the joining player
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                Item lamp = BeltState.getLamp(other);
                BeltNetworking.sendTo(joining, other.getUuid(), lamp);
            }
        });

        // On disconnect, persist the current state for that player
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity leaving = handler.getPlayer();
            Item lamp = BeltState.getLamp(leaving);
            BeltLanternSave.get(server).set(leaving.getUuid(), lamp);
        });

        // Handle lamp drop/persistence on death and sync after respawn
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return;
            boolean keep = oldPlayer.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
            BeltLanternServer.handleDeath(oldPlayer, keep);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return;
            Item lamp = BeltState.getLamp(newPlayer);
            BeltNetworking.broadcastBeltState(newPlayer, lamp);
        });
    }

    // no-op methods removed: toggling now handled via network payload
}
