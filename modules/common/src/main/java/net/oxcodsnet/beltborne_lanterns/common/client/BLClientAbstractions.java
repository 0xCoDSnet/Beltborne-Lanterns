package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Small indirection layer for platform-specific client bits.
 * Platforms set the providers at client init.
 */
public final class BLClientAbstractions {
    private static volatile Function<PlayerEntity, Item> lampProvider = p -> null;
    private static final AtomicBoolean DEBUG_FLAG = new AtomicBoolean(false);
    private BLClientAbstractions() {}

    public static void init(Function<PlayerEntity, Item> lampFunc) {
        lampProvider = Objects.requireNonNull(lampFunc);
    }

    public static boolean clientHasLantern(PlayerEntity player) {
        return lampProvider.apply(player) != null;
    }

    public static Item clientLamp(PlayerEntity player) {
        return lampProvider.apply(player);
    }

    public static boolean isDebugDrawEnabled() {
        return DEBUG_FLAG.get();
    }

    public static void setDebugDrawEnabled(boolean enabled) {
        DEBUG_FLAG.set(enabled);
    }
}
