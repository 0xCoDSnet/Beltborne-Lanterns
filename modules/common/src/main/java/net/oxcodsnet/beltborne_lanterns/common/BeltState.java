package net.oxcodsnet.beltborne_lanterns.common;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime-only per-player state: whether a lantern is "equipped on the belt".
 * Minimal demo: no persistence, no components; just memory + networking.
 */
public final class BeltState {
    private static final Set<UUID> PLAYERS_WITH_LANTERN = ConcurrentHashMap.newKeySet();

    private BeltState() {}

    public static boolean hasLantern(UUID uuid) {
        return PLAYERS_WITH_LANTERN.contains(uuid);
    }

    public static boolean hasLantern(PlayerEntity player) {
        return hasLantern(player.getUuid());
    }

    public static void setHasLantern(UUID uuid, boolean hasLantern) {
        if (hasLantern) {
            PLAYERS_WITH_LANTERN.add(uuid);
        } else {
            PLAYERS_WITH_LANTERN.remove(uuid);
        }
    }

    public static void setHasLantern(PlayerEntity player, boolean hasLantern) {
        setHasLantern(player.getUuid(), hasLantern);
    }
}

