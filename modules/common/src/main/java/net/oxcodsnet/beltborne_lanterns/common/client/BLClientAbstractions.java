package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Small indirection layer for platform-specific client bits.
 * Platforms set the providers at client init.
 */
public final class BLClientAbstractions {
    private static volatile Predicate<PlayerEntity> hasLantern = p -> false;
    private static volatile Supplier<Boolean> debugEnabled = () -> false;

    private BLClientAbstractions() {}

    public static void init(Predicate<PlayerEntity> hasLanternPredicate, Supplier<Boolean> debugSupplier) {
        hasLantern = Objects.requireNonNull(hasLanternPredicate);
        debugEnabled = Objects.requireNonNull(debugSupplier);
    }

    public static boolean clientHasLantern(PlayerEntity player) {
        return hasLantern.test(player);
    }

    public static boolean isDebugDrawEnabled() {
        return debugEnabled.get();
    }
}

