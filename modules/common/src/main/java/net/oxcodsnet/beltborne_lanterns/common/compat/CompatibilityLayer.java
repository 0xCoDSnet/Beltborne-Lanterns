package net.oxcodsnet.beltborne_lanterns.common.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

/**
 * An SPI interface for compatibility layers.
 * All methods should have a default implementation that does nothing or returns an empty value
 * to ensure that the main mod does not crash if a compatibility layer is not implemented correctly.
 */
public interface CompatibilityLayer {
    /**
     * A unique ID for the compatibility layer.
     * This is used to identify the compatibility layer and to check if it is loaded.
     * The ID should be the same as the mod ID of the mod that the compatibility layer is for.
     * @return The unique ID of the compatibility layer.
     */
    String getModId();

    /**
     * Called when the mod is initialized.
     * This is where the compatibility layer should register its event listeners and other things.
     * This method should only be called if the mod that the compatibility layer is for is loaded.
     */
    default void onInitialize() {}

    /**
     * Called when the player presses the toggle key.
     * This method should try to toggle the lantern in the compatibility mod's slot.
     * @param player The player who pressed the key.
     * @return True if the lantern was toggled, false otherwise.
     */
    default boolean tryToggleLantern(ServerPlayerEntity player) {
        return false;
    }

    /**
     * Called when the player toggles the lantern on.
     * This method should sync the lantern to the compatibility mod's slot.
     * @param player The player who toggled the lantern on.
     */
    default void syncToggleOn(ServerPlayerEntity player) {}

    /**
     * Called when the player joins the server.
     * This method should get the lantern from the compatibility mod's slot.
     * @param player The player who joined the server.
     * @return The lantern from the compatibility mod's slot, or an empty optional if there is no lantern.
     */
    default Optional<ItemStack> getBeltStack(ServerPlayerEntity player) {
        return Optional.empty();
    }
}
