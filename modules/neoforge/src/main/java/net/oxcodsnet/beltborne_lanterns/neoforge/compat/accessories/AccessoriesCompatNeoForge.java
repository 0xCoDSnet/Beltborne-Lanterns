package net.oxcodsnet.beltborne_lanterns.neoforge.compat.accessories;

import io.wispforest.accessories.api.events.AccessoryChangeCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;
import net.oxcodsnet.beltborne_lanterns.neoforge.BeltNetworking;

/**
 * Accessories (WispForest) integration for NeoForge.
 */
public final class AccessoriesCompatNeoForge {
    private AccessoriesCompatNeoForge() {}
    private static final String BELT = "belt";
    private static boolean isBeltSlot(SlotReference ref) {
        String name = ref.slotName();
        return BELT.equals(name) || (name != null && name.endsWith(":" + BELT));
    }
    private static final java.util.Set<java.util.UUID> SYNCING = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public static void init() {
        AccessoryChangeCallback.EVENT.register((prev, now, ref, change) -> {
            if (!(ref.entity() instanceof ServerPlayerEntity player)) return;
            if (!isBeltSlot(ref)) return;

            boolean prevIsLamp = LampRegistry.isLamp(prev);
            boolean newIsLamp = LampRegistry.isLamp(now);

            if (!prevIsLamp && newIsLamp) {
                if (BeltState.hasLamp(player) && !SYNCING.contains(player.getUuid())) {
                    ItemStack current = BeltState.getLampStack(player);
                    boolean same = current != null && ItemStack.areEqual(current, now);
                    if (!same && !player.isCreative() && current != null && !current.isEmpty()) {
                        player.giveItemStack(current);
                    }
                }
                BeltState.setLamp(player, now);
                BeltLanternSave.get(player.server).set(player.getUuid(), now);
                BeltNetworking.broadcastBeltState(player, now.getItem());
            } else if (prevIsLamp && !newIsLamp) {
                BeltState.setLamp(player, (Item) null);
                BeltLanternSave.get(player.server).set(player.getUuid(), (ItemStack) null);
                BeltNetworking.broadcastBeltState(player, null);
            } else if (prevIsLamp && newIsLamp) {
                BeltState.setLamp(player, now);
                BeltLanternSave.get(player.server).set(player.getUuid(), now);
                BeltNetworking.broadcastBeltState(player, now.getItem());
            }
        });

        BLMod.LOGGER.info("Accessories integration active [NeoForge]");
    }

    public static boolean tryToggleViaAccessories(ServerPlayerEntity player) {
        SlotReference ref = SlotReference.of(player, BELT, 0);
        if (!ref.isValid()) return false;
        ItemStack stack = ref.getStack();
        if (LampRegistry.isLamp(stack)) {
            ItemStack toReturn = stack.copy();
            ref.setStack(ItemStack.EMPTY);
            if (!player.isCreative() && !toReturn.isEmpty()) {
                player.giveItemStack(toReturn);
            }
            return true;
        }
        return false;
    }

    /** Returns the current stack in the Accessories belt slot, or ItemStack.EMPTY if absent. */
    public static ItemStack getBeltStack(ServerPlayerEntity player) {
        SlotReference ref = SlotReference.of(player, BELT, 0);
        if (!ref.isValid()) return ItemStack.EMPTY;
        return ref.getStack();
    }

    /** If toggled ON via B, mirror to Accessories belt slot (if empty). */
    public static void syncToggleOnToAccessories(ServerPlayerEntity player) {
        SlotReference ref = SlotReference.of(player, BELT, 0);
        if (!ref.isValid()) return;
        if (!ref.getStack().isEmpty()) return;
        ItemStack stored = BeltState.getLampStack(player);
        if (stored == null || stored.isEmpty()) return;
        SYNCING.add(player.getUuid());
        try {
            ref.setStack(stored);
        } finally {
            SYNCING.remove(player.getUuid());
        }
    }

    // No registration needed when acceptance is driven by tags; we only listen for changes
}
