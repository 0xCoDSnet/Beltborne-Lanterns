package net.oxcodsnet.beltborne_lanterns.fabric.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLConfigs;

public final class BLConfigHolder {
    private static ConfigHolder<BLClientConfig> HOLDER;

    private BLConfigHolder() {}

    public static void init() {
        if (HOLDER != null) return;
        AutoConfig.register(BLClientConfig.class, GsonConfigSerializer::new);
        HOLDER = AutoConfig.getConfigHolder(BLClientConfig.class);
        // Seed common snapshot
        BLConfigs.set(copyToCommon(HOLDER.getConfig()));
        // Keep common snapshot synced on save
        HOLDER.registerSaveListener((h, cfg) -> {
            BLConfigs.set(copyToCommon(cfg));
            return me.shedaniel.autoconfig.ConfigData.saveAll(true);
        });
    }

    public static BLClientConfig get() {
        if (HOLDER == null) init();
        return HOLDER.getConfig();
    }

    private static BLConfig copyToCommon(BLClientConfig c) {
        BLConfig out = new BLConfig();
        out.offsetX100 = c.offsetX100;
        out.offsetY100 = c.offsetY100;
        out.offsetZ100 = c.offsetZ100;
        out.pivotX100 = c.pivotX100;
        out.pivotY100 = c.pivotY100;
        out.pivotZ100 = c.pivotZ100;
        out.rotXDeg = c.rotXDeg;
        out.rotYDeg = c.rotYDeg;
        out.rotZDeg = c.rotZDeg;
        out.scale100 = c.scale100;
        return out;
    }
}
