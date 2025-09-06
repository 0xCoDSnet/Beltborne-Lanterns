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
            // Copy the exact stack (count=1) BEFORE decrementing, to preserve NBT when count==1
            ItemStack equipped = stackInHand.copyWithCount(1);
            if (!creative) {
                stackInHand.decrement(1);
                // Ensure inventory updates are propagated in survival
                player.getInventory().markDirty();
            }
            // Store the exact stack (count=1) to preserve NBT/enchantments/etc.
            BeltState.setLamp(player, equipped);
            // Persist full stack including NBT for cross-restart restore
            BeltLanternSave.get(player.server).set(player.getUuid(), equipped);
            return item;
        } else {
            if (!creative) {
                // Return the exact stored stack with NBT back to the player
                ItemStack stored = BeltState.getLampStack(player);
                if (stored != null && !stored.isEmpty()) {
                    player.giveItemStack(stored);
                } else {
                    // Fallback: at least return the plain item
                    player.giveItemStack(new ItemStack(current));
                }
            }
            // Clear state and persistence
            BeltState.setLamp(player, (ItemStack) null);
            BeltLanternSave.get(player.server).set(player.getUuid(), (ItemStack) null);
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
        // Drop the exact stored stack with NBT
        ItemStack stored = BeltState.getLampStack(player);
        if (stored != null && !stored.isEmpty()) {
            player.dropStack(player.getServerWorld(), stored);
        } else {
            ItemStack stack = new ItemStack(lamp);
            player.dropStack(player.getServerWorld(), stack);
        }
        BeltState.setLamp(player, (ItemStack) null);
        BeltLanternSave.get(player.server).set(player.getUuid(), (ItemStack) null);
        return null;
    }

    /**
     * Overload used by loader-specific events that provide both old and new players.
     * Drops from the old player's location when keepInventory is false; otherwise preserves
     * the lamp stack onto the new player.
     */
    public static void handleDeath(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean keepInventory) {
        Item lamp = BeltState.getLamp(oldPlayer);
        if (lamp == null) {
            // Ensure new player has no stale state
            BeltState.setLamp(newPlayer, (ItemStack) null);
            return;
        }
        if (keepInventory) {
            ItemStack stored = BeltState.getLampStack(oldPlayer);
            // Transfer the exact stack (including NBT) to the new player
            BeltState.setLamp(newPlayer, stored);
            BeltLanternSave.get(newPlayer.server).set(newPlayer.getUuid(), stored);
            return;
        }
        // Drop from old player's position and clear state
        ItemStack stored = BeltState.getLampStack(oldPlayer);
        if (stored != null && !stored.isEmpty()) {
            oldPlayer.dropStack(oldPlayer.getServerWorld(), stored);
        } else {
            oldPlayer.dropStack(oldPlayer.getServerWorld(), new ItemStack(lamp));
        }
        BeltState.setLamp(oldPlayer, (ItemStack) null);
        BeltLanternSave.get(oldPlayer.server).set(oldPlayer.getUuid(), (ItemStack) null);
        // Ensure new player does not carry over state
        BeltState.setLamp(newPlayer, (ItemStack) null);
    }
}
