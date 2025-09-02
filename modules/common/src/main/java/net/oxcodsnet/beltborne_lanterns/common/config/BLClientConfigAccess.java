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
            BLClientConfig clientConfig = HOLDER.getConfig();

            // Migrate from 1/100 to 1/1000 precision if old values are detected
            boolean looksLikeOldScale =
                    Math.abs(clientConfig.offsetX100) <= 200 &&
                    Math.abs(clientConfig.offsetY100) <= 200 &&
                    Math.abs(clientConfig.offsetZ100) <= 200 &&
                    Math.abs(clientConfig.pivotX100)  <= 200 &&
                    Math.abs(clientConfig.pivotY100)  <= 200 &&
                    Math.abs(clientConfig.pivotZ100)  <= 200 &&
                    clientConfig.scale100 <= 100;
            if (looksLikeOldScale) {
                clientConfig.offsetX100 *= 10;
                clientConfig.offsetY100 *= 10;
                clientConfig.offsetZ100 *= 10;
                clientConfig.pivotX100  *= 10;
                clientConfig.pivotY100  *= 10;
                clientConfig.pivotZ100  *= 10;
                clientConfig.scale100   *= 10;
                HOLDER.save();
            }
            clientConfig.extraLampLight.clear(); // Clear existing to avoid duplicates on re-init

            java.util.Map<String, BLClientConfig.ExtraLampEntry> tempMap = new java.util.LinkedHashMap<>();

            // Add/overwrite lamps from BLLampConfigAccess (server-synced/custom)
            BLLampConfigAccess.get().extraLampLight.forEach(entry -> {
                tempMap.put(entry.id, new BLClientConfig.ExtraLampEntry(entry.id, entry.luminance));
            });

            // Convert tempMap to List and set it to clientConfig.extraLampLight
            clientConfig.extraLampLight.addAll(tempMap.values());
            // Seed common snapshot
            BLConfigs.set(copyToCommon(HOLDER.getConfig()));
            // Keep snapshot synced on save
            HOLDER.registerSaveListener((h, cfg) -> {
                BLConfigs.set(copyToCommon(cfg));
                BLLampConfig lampCfg = BLLampConfigAccess.get();
                lampCfg.extraLampLight.clear();
                cfg.extraLampLight.forEach(entry -> lampCfg.extraLampLight.add(new BLClientConfig.ExtraLampEntry(entry.id, entry.luminance)));
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
