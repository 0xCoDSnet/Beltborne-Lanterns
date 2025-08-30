package net.oxcodsnet.beltborne_lanterns.fabric.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class BLConfigHolder {
    private static BLClientConfig INSTANCE;

    private BLConfigHolder() {}

    public static void init() {
        AutoConfig.register(BLClientConfig.class, GsonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(BLClientConfig.class).getConfig();
    }

    public static BLClientConfig get() {
        if (INSTANCE == null) init();
        return INSTANCE;
    }
}

