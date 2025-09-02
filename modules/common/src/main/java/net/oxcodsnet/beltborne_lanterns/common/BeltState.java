package net.oxcodsnet.beltborne_lanterns.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime-only per-player state: which lamp (with NBT) is
 * "equipped on the belt". Stored as an ItemStack to preserve all data.
 */
public final class BeltState {
    private static final Map<UUID, ItemStack> PLAYER_LAMPS = new ConcurrentHashMap<>();

    private BeltState() {}

    public static boolean hasLamp(UUID uuid) {
        return PLAYER_LAMPS.containsKey(uuid);
    }

    public static boolean hasLamp(PlayerEntity player) {
        return hasLamp(player.getUuid());
    }

    /**
     * Returns the lamp item type currently equipped, or null.
     * Kept for convenience when only item identity is required (e.g., networking/FX).
     */
    public static Item getLamp(UUID uuid) {
        ItemStack stack = PLAYER_LAMPS.get(uuid);
        return stack != null ? stack.getItem() : null;
    }

    /**
     * Returns the lamp item type currently equipped, or null.
     */
    public static Item getLamp(PlayerEntity player) {
        return getLamp(player.getUuid());
    }

    /**
     * Returns a copy of the stored lamp stack (count=as stored), or null.
     */
    public static ItemStack getLampStack(UUID uuid) {
        ItemStack stack = PLAYER_LAMPS.get(uuid);
        return stack != null ? stack.copy() : null;
    }

    public static ItemStack getLampStack(PlayerEntity player) {
        return getLampStack(player.getUuid());
    }

    /**
     * Sets the equipped lamp to the given stack (stored as a single-item copy), or clears when null.
     */
    public static void setLamp(UUID uuid, ItemStack lamp) {
        if (lamp != null && !lamp.isEmpty()) {
            PLAYER_LAMPS.put(uuid, lamp.copyWithCount(1));
        } else {
            PLAYER_LAMPS.remove(uuid);
        }
    }

    public static void setLamp(PlayerEntity player, ItemStack lamp) {
        setLamp(player.getUuid(), lamp);
    }

    /**
     * Convenience: sets from item type only (no NBT).
     */
    public static void setLamp(UUID uuid, Item lamp) {
        if (lamp != null) {
            PLAYER_LAMPS.put(uuid, new ItemStack(lamp));
        } else {
            PLAYER_LAMPS.remove(uuid);
        }
    }

    public static void setLamp(PlayerEntity player, Item lamp) {
        setLamp(player.getUuid(), lamp);
    }
}

