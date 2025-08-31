package net.oxcodsnet.beltborne_lanterns.common.config;

import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
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
            BLLampConfig lampConfig = HOLDER.getConfig();

            java.util.Map<String, BLClientConfig.ExtraLampEntry> combinedLamps = new java.util.LinkedHashMap<>();

            // Add lamps from LampRegistry (mod-registered and tagged)
            LampRegistry.items().forEach(item -> {
                String id = LampRegistry.getId(item).toString();
                int luminance = LampRegistry.getLuminance(item);
                combinedLamps.put(id, new BLClientConfig.ExtraLampEntry(id, luminance));
            });

            // Add/overwrite custom entries from the config file
            lampConfig.extraLampLight.forEach(entry -> {
                combinedLamps.put(entry.id, new BLClientConfig.ExtraLampEntry(entry.id, entry.luminance));
            });

            // Clear the original list and add all combined lamps
            lampConfig.extraLampLight.clear();
            lampConfig.extraLampLight.addAll(combinedLamps.values());
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
