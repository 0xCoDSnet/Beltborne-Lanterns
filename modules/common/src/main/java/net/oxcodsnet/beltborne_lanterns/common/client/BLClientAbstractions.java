package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Small indirection layer for platform-specific client bits.
 * Platforms set the providers at client init.
 */
public final class BLClientAbstractions {
    private static volatile Function<PlayerEntity, Item> lampProvider = p -> null;
    private static final AtomicBoolean DEBUG_FLAG = new AtomicBoolean(false);
    private static volatile Supplier<Boolean> debugEnabled = DEBUG_FLAG::get;
    private static volatile Consumer<Boolean> debugSetter = DEBUG_FLAG::set;

    private BLClientAbstractions() {}

    public static void init(Function<PlayerEntity, Item> lampFunc, Supplier<Boolean> debugSupplier) {
        lampProvider = Objects.requireNonNull(lampFunc);
        // Keep using the internal atomic debug flag; ignore external supplier to avoid self-recursion.
    }

    public static void setDebugSetter(Consumer<Boolean> setter) {
        debugSetter = Objects.requireNonNull(setter);
    }

    public static boolean clientHasLantern(PlayerEntity player) {
        return lampProvider.apply(player) != null;
    }

    public static Item clientLamp(PlayerEntity player) {
        return lampProvider.apply(player);
    }

    public static boolean isDebugDrawEnabled() {
        return debugEnabled.get();
    }

    public static void setDebugDrawEnabled(boolean enabled) {
        debugSetter.accept(enabled);
    }
}
