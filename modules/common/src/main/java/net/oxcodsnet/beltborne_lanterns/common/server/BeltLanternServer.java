package net.oxcodsnet.beltborne_lanterns.common.server;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;

/**
 * Common server-side logic for toggling the belt lamp state.
 *
 * Platform-specific code should call {@link #toggleLantern(ServerPlayerEntity, ItemStack)}
 * then perform its own broadcasting.
 */
public final class BeltLanternServer {
    private BeltLanternServer() {}

    /**
     * Toggles the belt lamp for the player and updates persistence + inventory.
     *
     * @param player      the server player
     * @param stackInHand the stack being used (to consume/return lamp when not creative)
     * @return the lamp item now equipped, or {@code null} if unequipped
     */
    public static Item toggleLantern(ServerPlayerEntity player, ItemStack stackInHand) {
        Item current = BeltState.getLamp(player);
        boolean creative = player.isCreative();
        if (current == null) {
            Item item = stackInHand.getItem();
            if (!LampRegistry.isLamp(item)) return null;
            if (!creative) {
                stackInHand.decrement(1);
            }
            BeltState.setLamp(player, item);
            BeltLanternSave.get(player.server).set(player.getUuid(), item);
            return item;
        } else {
            if (!creative) {
                player.giveItemStack(new ItemStack(current));
            }
            BeltState.setLamp(player, null);
            BeltLanternSave.get(player.server).set(player.getUuid(), null);
            return null;
        }
    }

    /**
     * Handles player death logic with respect to the belt lantern.
     * If keepInventory is disabled the lamp is dropped and state cleared,
     * otherwise the lamp remains equipped.
     *
     * @param player        the dying player
     * @param keepInventory whether the KEEP_INVENTORY gamerule is enabled
     * @return the lamp item that remains equipped, or {@code null} if none
     */
    public static Item handleDeath(ServerPlayerEntity player, boolean keepInventory) {
        Item lamp = BeltState.getLamp(player);
        if (lamp == null) return null;
        if (keepInventory) {
            return lamp;
        }
        player.giveOrDropStack(new ItemStack(lamp));
        BeltState.setLamp(player, null);
        BeltLanternSave.get(player.server).set(player.getUuid(), null);
        return null;
    }
}

