package net.oxcodsnet.beltborne_lanterns.common.config;

import java.util.Objects;

/**
 * Global access to the current config snapshot on the client.
 * Platform loaders should update this when their UI/config changes.
 */
public final class BLConfigs {
    private static volatile BLConfig INSTANCE = new BLConfig();

    private BLConfigs() {}

    public static BLConfig get() {
        return INSTANCE;
    }

    public static void set(BLConfig cfg) {
        INSTANCE = Objects.requireNonNull(cfg);
    }
}

