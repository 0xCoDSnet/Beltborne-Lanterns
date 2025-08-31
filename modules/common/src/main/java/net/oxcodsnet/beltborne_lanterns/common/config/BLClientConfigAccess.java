package net.oxcodsnet.beltborne_lanterns.common.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.util.ActionResult;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;

/**
 * Common accessor for the client config using Cloth AutoConfig.
 * Provides lazy registration and keeps the common BLConfigs snapshot in sync.
 */
public final class BLClientConfigAccess {
    private static volatile ConfigHolder<BLClientConfig> HOLDER;

    private BLClientConfigAccess() {}

    private static void ensureInit() {
        if (HOLDER != null) return;
        synchronized (BLClientConfigAccess.class) {
            if (HOLDER != null) return;
            AutoConfig.register(BLClientConfig.class, GsonConfigSerializer::new);
            HOLDER = AutoConfig.getConfigHolder(BLClientConfig.class);
            // Seed lamp config from shared lamp config
            HOLDER.getConfig().extraLampLight.putAll(BLLampConfigAccess.get().extraLampLight);
            // Seed common snapshot
            BLConfigs.set(copyToCommon(HOLDER.getConfig()));
            // Keep snapshot synced on save
            HOLDER.registerSaveListener((h, cfg) -> {
                BLConfigs.set(copyToCommon(cfg));
                BLLampConfig lampCfg = BLLampConfigAccess.get();
                lampCfg.extraLampLight.clear();
                lampCfg.extraLampLight.putAll(cfg.extraLampLight);
                BLLampConfigAccess.save();
                LampRegistry.init();
                return ActionResult.SUCCESS;
            });
        }
    }

    public static BLClientConfig get() {
        ensureInit();
        return HOLDER.getConfig();
    }

    public static void save() {
        ensureInit();
        HOLDER.save();
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

