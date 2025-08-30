package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loader-agnostic client-side cache of players who currently have a belt lantern.
 * Platform layers should call {@link #setHas(UUID, boolean)} from their network handlers.
 */
public final class ClientBeltPlayers {
    private static final Set<UUID> BELT_PLAYERS = ConcurrentHashMap.newKeySet();

    private ClientBeltPlayers() {}

    public static void setHas(UUID uuid, boolean has) {
        if (has) BELT_PLAYERS.add(uuid); else BELT_PLAYERS.remove(uuid);
    }

    public static boolean hasLantern(PlayerEntity player) {
        return BELT_PLAYERS.contains(player.getUuid());
    }
}

