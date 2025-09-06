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
import net.oxcodsnet.beltborne_lanterns.common.config.BLLampConfigAccess;
import net.oxcodsnet.beltborne_lanterns.common.network.LampConfigSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.common.server.BeltLanternServer;
import io.wispforest.accessories.api.components.AccessoriesDataComponents;
import io.wispforest.accessories.api.components.AccessorySlotValidationComponent;

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
                // If Accessories is present and the belt slot holds a lamp, unequip from there
                // to provide a unified toggle interface.
                boolean handledByAccessories = false;
                try {
                    // Call only if the compat class is present in the classpath; it's safe since we only load it when Accessories is installed.
                    handledByAccessories = net.oxcodsnet.beltborne_lanterns.fabric.compat.AccessoriesCompatFabric.tryToggleViaAccessories(player);
                } catch (Throwable ignored) {
                    // Accessories not installed or API unavailable; fall through to default logic
                }

                if (handledByAccessories) return;

                ItemStack stack = player.getMainHandStack();
                boolean hasLamp = BeltState.hasLamp(player);
                if (!hasLamp && !LampRegistry.isLamp(stack)) {
                    stack = player.getOffHandStack();
                    if (!LampRegistry.isLamp(stack)) return;
                }
                Item nowHas = BeltLanternServer.toggleLantern(player, stack);
                BeltNetworking.broadcastBeltState(player, nowHas);
                if (nowHas != null) {
                    try {
                        net.oxcodsnet.beltborne_lanterns.fabric.compat.AccessoriesCompatFabric.syncToggleOnToAccessories(player);
                    } catch (Throwable ignored) {
                        // Accessories not installed — ignore
                    }
                }
            });
        });

        // When a player joins, sync known belt states of all players to them and restore theirs
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joining = handler.getPlayer();
            // Restore from persistent save (full stack with NBT)
            var persistedStack = BeltLanternSave.get(server).getStack(joining.getUuid());

            // If Accessories belt slot already has a lamp, prefer that as source of truth
            try {
                var slotStack = net.oxcodsnet.beltborne_lanterns.fabric.compat.AccessoriesCompatFabric.getBeltStack(joining);
                if (LampRegistry.isLamp(slotStack)) {
                    persistedStack = slotStack;
                }
            } catch (Throwable ignored) {
                // Accessories not installed — ignore
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

            // Ensure inventory items are marked as valid for the belt slot via component validator
            try { ensureBeltValidationOnLamps(joining); } catch (Throwable ignored) {}
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
            try { net.oxcodsnet.beltborne_lanterns.fabric.compat.AccessoriesCompatFabric.refreshRegisteredAccessories(); } catch (Throwable ignored) {}
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            LampRegistry.init();
            try { net.oxcodsnet.beltborne_lanterns.fabric.compat.AccessoriesCompatFabric.refreshRegisteredAccessories(); } catch (Throwable ignored) {}
            // After tags reload, ensure all online players' inventories carry the belt validation component
            try {
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) ensureBeltValidationOnLamps(p);
            } catch (Throwable ignored) {}
        });
    }

    private static void ensureBeltValidationOnLamps(ServerPlayerEntity player) {
        var inv = player.getInventory();
        int size = inv.size();
        for (int i = 0; i < size; i++) {
            var stack = inv.getStack(i);
            if (stack == null || stack.isEmpty()) continue;
            if (!LampRegistry.isLamp(stack)) continue;
            var comp = stack.get(AccessoriesDataComponents.SLOT_VALIDATION);
            // addValidSlot returns a new instance; start from EMPTY if none
            var withBelt = (comp == null ? AccessorySlotValidationComponent.EMPTY : comp).addValidSlot("accessories:belt");
            stack.set(AccessoriesDataComponents.SLOT_VALIDATION, withBelt);
        }
    }
}
