package net.oxcodsnet.beltborne_lanterns.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime-only per-player state: which lamp item (if any) is
 * "equipped on the belt". Minimal demo: no persistence, no components;
 * just memory + networking.
 */
public final class BeltState {
    private static final Map<UUID, Item> PLAYER_LAMPS = new ConcurrentHashMap<>();

    private BeltState() {}

    public static boolean hasLamp(UUID uuid) {
        return PLAYER_LAMPS.containsKey(uuid);
    }

    public static boolean hasLamp(PlayerEntity player) {
        return hasLamp(player.getUuid());
    }

    public static Item getLamp(UUID uuid) {
        return PLAYER_LAMPS.get(uuid);
    }

    public static Item getLamp(PlayerEntity player) {
        return getLamp(player.getUuid());
    }

    public static void setLamp(UUID uuid, Item lamp) {
        if (lamp != null) {
            PLAYER_LAMPS.put(uuid, lamp);
        } else {
            PLAYER_LAMPS.remove(uuid);
        }
    }

    public static void setLamp(PlayerEntity player, Item lamp) {
        setLamp(player.getUuid(), lamp);
    }
}

