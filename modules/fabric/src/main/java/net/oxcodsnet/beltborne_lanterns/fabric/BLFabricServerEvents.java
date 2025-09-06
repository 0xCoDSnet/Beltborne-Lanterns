package net.oxcodsnet.beltborne_lanterns.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayerRegistry;
import net.oxcodsnet.beltborne_lanterns.common.config.BLLampConfigAccess;
import net.oxcodsnet.beltborne_lanterns.common.network.LampConfigSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.common.server.BeltLanternServer;

import java.util.LinkedHashMap;

/**
 * Handles all server-side event registrations for Fabric.
 */
public final class BLFabricServerEvents {
    private BLFabricServerEvents() {}

    public static void initialize() {
        // Handle client toggle requests
        ServerPlayNetworking.registerGlobalReceiver(ToggleLanternPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                // Try to toggle lantern via compatibility layers
                for (var layer : CompatibilityLayerRegistry.getLayers()) {
                    if (layer.tryToggleLantern(player)) return;
                }

                ItemStack stack = player.getMainHandStack();
                boolean hasLamp = BeltState.hasLamp(player);
                if (!hasLamp && !LampRegistry.isLamp(stack)) {
                    stack = player.getOffHandStack();
                    if (!LampRegistry.isLamp(stack)) return;
                }
                Item nowHas = BeltLanternServer.toggleLantern(player, stack);
                BeltNetworking.broadcastBeltState(player, nowHas);
                if (nowHas != null) {
                    // Sync to compatibility layers
                    for (var layer : CompatibilityLayerRegistry.getLayers()) {
                        layer.syncToggleOn(player);
                    }
                }
            });
        });

        // When a player joins, sync known belt states of all players to them and restore theirs
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joining = handler.getPlayer();
            // Restore from persistent save (full stack with NBT)
            var persistedStack = BeltLanternSave.get(server).getStack(joining.getUuid());

            // If a compatibility layer has a belt stack, prefer that as source of truth
            for (var layer : CompatibilityLayerRegistry.getLayers()) {
                var slotStack = layer.getBeltStack(joining);
                if (slotStack.isPresent() && LampRegistry.isLamp(slotStack.get())) {
                    persistedStack = slotStack.get();
                    break;
                }
            }

            Item persisted = persistedStack != null ? persistedStack.getItem() : null;
            BeltState.setLamp(joining, persistedStack);
            // Tell everyone (and self) about joining player's state (by item type)
            BeltNetworking.broadcastBeltState(joining, persisted);
            // If on a dedicated server, send its lamp config to the joining player.
            // In single player, the client's config is trusted as the source of truth.
            if (server.isDedicated()) {
                var lampMap = new LinkedHashMap<Identifier, Integer>();
                BLLampConfigAccess.get().extraLampLight.forEach(entry -> {
                    Identifier id = Identifier.tryParse(entry.id);
                    if (id != null) lampMap.put(id, entry.luminance);
                });
                ServerPlayNetworking.send(joining, new LampConfigSyncPayload(lampMap));
            }
            // Send existing players' states to the joining player
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                Item lamp = BeltState.getLamp(other);
                BeltNetworking.sendTo(joining, other.getUuid(), lamp);
            }

        });

        // On disconnect, persist the current state for that player
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity leaving = handler.getPlayer();
            // Persist full stack with NBT on disconnect
            BeltLanternSave.get(server).set(leaving.getUuid(), BeltState.getLampStack(leaving));
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

        // Lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LampRegistry.init();
            // Write runtime datapack with tag entries from config and suggest reload if changed
            boolean dpChanged = net.oxcodsnet.beltborne_lanterns.common.datapack.BLRuntimeDataPack.writeOrUpdate(server);
            // Accessories acceptance is handled via tags (runtime datapack)
            if (dpChanged) {
                // Attempt a one-time /reload to apply the new datapack
                try {
                    server.getCommandManager().executeWithPrefix(server.getCommandSource(), "reload");
                } catch (Throwable t) {
                    net.oxcodsnet.beltborne_lanterns.BLMod.LOGGER.info("Runtime datapack updated — please run /reload to apply");
                }
            }
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            LampRegistry.init();
            // Keep runtime datapack in sync on reload
            net.oxcodsnet.beltborne_lanterns.common.datapack.BLRuntimeDataPack.writeOrUpdate(server);
            // Accessories acceptance is handled via tags (runtime datapack)
        });
    }
}
