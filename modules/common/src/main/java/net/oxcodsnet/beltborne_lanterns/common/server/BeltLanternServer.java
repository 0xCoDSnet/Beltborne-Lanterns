package net.oxcodsnet.beltborne_lanterns.common.server;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.persistence.BeltLanternSave;

/**
 * Common server-side logic for toggling the belt lantern state.
 *
 * Platform-specific code should call {@link #toggleLantern(ServerPlayerEntity, ItemStack)}
 * then perform its own broadcasting.
 */
public final class BeltLanternServer {
    private BeltLanternServer() {}

    /**
     * Toggles the belt lantern for the player and updates persistence + inventory.
     *
     * @param player      the server player
     * @param stackInHand the stack being used (to consume/return lantern when not creative)
     * @return the new state after toggling (true if now has lantern)
     */
    public static boolean toggleLantern(ServerPlayerEntity player, ItemStack stackInHand) {
        boolean has = BeltState.hasLantern(player);
        boolean creative = player.isCreative();
        if (!has) {
            if (!creative && stackInHand.isOf(Items.LANTERN)) {
                stackInHand.decrement(1);
            }
            BeltState.setHasLantern(player, true);
            BeltLanternSave.get(player.server).set(player.getUuid(), true);
            return true;
        } else {
            if (!creative) {
                player.giveItemStack(new ItemStack(Items.LANTERN));
            }
            BeltState.setHasLantern(player, false);
            BeltLanternSave.get(player.server).set(player.getUuid(), false);
            return false;
        }
    }
}

