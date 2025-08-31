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
}

