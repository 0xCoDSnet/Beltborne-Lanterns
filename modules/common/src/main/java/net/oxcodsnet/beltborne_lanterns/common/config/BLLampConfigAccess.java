package net.oxcodsnet.beltborne_lanterns.common.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

/**
 * Accessor for the lamp config using Cloth AutoConfig.
 */
public final class BLLampConfigAccess {
    private static volatile ConfigHolder<BLLampConfig> HOLDER;

    private BLLampConfigAccess() {}

    private static void ensureInit() {
        if (HOLDER != null) return;
        synchronized (BLLampConfigAccess.class) {
            if (HOLDER != null) return;
            AutoConfig.register(BLLampConfig.class, GsonConfigSerializer::new);
            HOLDER = AutoConfig.getConfigHolder(BLLampConfig.class);
        }
    }

    public static BLLampConfig get() {
        ensureInit();
        return HOLDER.getConfig();
    }

    public static void save() {
        ensureInit();
        HOLDER.save();
    }
}
