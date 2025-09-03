package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loader-agnostic client-side cache of players who currently have a belt lamp.
 * Platform layers should call {@link #setLamp(UUID, Item)} from their network handlers.
 */
public final class ClientBeltPlayers {
    private static final Map<UUID, Item> BELT_PLAYERS = new ConcurrentHashMap<>();

    private ClientBeltPlayers() {}

    public static void setLamp(UUID uuid, Item lamp) {
        if (lamp != null) BELT_PLAYERS.put(uuid, lamp); else BELT_PLAYERS.remove(uuid);
    }

    public static boolean hasLantern(PlayerEntity player) {
        return BELT_PLAYERS.containsKey(player.getUuid());
    }

    public static Item getLamp(PlayerEntity player) {
        return BELT_PLAYERS.get(player.getUuid());
    }

    public static Item getLamp(UUID uuid) {
        return BELT_PLAYERS.get(uuid);
    }

    public static void clear() {
        BELT_PLAYERS.clear();
    }
}
