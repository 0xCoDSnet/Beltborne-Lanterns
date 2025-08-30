package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Small indirection layer for platform-specific client bits.
 * Platforms set the providers at client init.
 */
public final class BLClientAbstractions {
    private static volatile Predicate<PlayerEntity> hasLantern = p -> false;
    private static final AtomicBoolean DEBUG_FLAG = new AtomicBoolean(false);
    private static volatile Supplier<Boolean> debugEnabled = DEBUG_FLAG::get;
    private static volatile Consumer<Boolean> debugSetter = DEBUG_FLAG::set;

    private BLClientAbstractions() {}

    public static void init(Predicate<PlayerEntity> hasLanternPredicate, Supplier<Boolean> debugSupplier) {
        hasLantern = Objects.requireNonNull(hasLanternPredicate);
        debugEnabled = Objects.requireNonNull(debugSupplier);
    }

    public static void setDebugSetter(Consumer<Boolean> setter) {
        debugSetter = Objects.requireNonNull(setter);
    }

    public static boolean clientHasLantern(PlayerEntity player) {
        return hasLantern.test(player);
    }

    public static boolean isDebugDrawEnabled() {
        return debugEnabled.get();
    }

    public static void setDebugDrawEnabled(boolean enabled) {
        debugSetter.accept(enabled);
    }
}
