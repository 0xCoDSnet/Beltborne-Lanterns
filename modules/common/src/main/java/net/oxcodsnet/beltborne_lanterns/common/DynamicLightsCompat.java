package net.oxcodsnet.beltborne_lanterns.common;

import net.minecraft.server.network.ServerPlayerEntity;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional integration with AtomicStryker's Dynamic Lights (NeoForge).
 * Implemented via reflection to keep the dependency optional.
 */
public final class DynamicLightsCompat {
    private static volatile boolean PRESENT = false;
    private static Class<?> lightInterface;
    private static Method addLightSource;
    private static Method removeLightSource;

    private static final Map<UUID, Object> ACTIVE = new ConcurrentHashMap<>();

    private DynamicLightsCompat() {}

    public static void init() {
        if (PRESENT) return;
        try {
            Class<?> dlClass = Class.forName("atomicstryker.dynamiclights.server.DynamicLights");
            lightInterface = Class.forName("atomicstryker.dynamiclights.server.IDynamicLightSource");
            addLightSource = dlClass.getMethod("addLightSource", lightInterface);
            removeLightSource = dlClass.getMethod("removeLightSource", lightInterface);
            PRESENT = true;
            BLMod.LOGGER.info("Dynamic lights: integrated via AtomicStryker DynamicLights API");
        } catch (Throwable t) {
            PRESENT = false;
            BLMod.LOGGER.debug("Dynamic lights: DynamicLights not present; skipping integration");
        }
    }

    public static void addFor(ServerPlayerEntity player) {
        if (!PRESENT || player == null) return;
        UUID id = player.getUuid();
        if (ACTIVE.containsKey(id)) return;

        Object proxy = Proxy.newProxyInstance(
                lightInterface.getClassLoader(),
                new Class<?>[]{lightInterface},
                (p, m, args) -> {
                    String name = m.getName();
                    // Ensure Object methods behave correctly on proxies
                    if ("equals".equals(name)) {
                        // Reference equality is what DynamicLights expects when removing
                        return Boolean.valueOf(p == (args != null && args.length == 1 ? args[0] : null));
                    } else if ("hashCode".equals(name)) {
                        return Integer.valueOf(System.identityHashCode(p));
                    } else if ("toString".equals(name)) {
                        return "BeltborneDLSource{" + player.getGameProfile().getName() + ":" + player.getUuid() + "}";
                    }
                    if ("getAttachmentEntity".equals(name)) {
                        return player; // runtime-mapped entity instance
                    } else if ("getLightLevel".equals(name)) {
                        var lamp = BeltState.getLamp(player);
                        return lamp != null ? LampRegistry.getLuminance(lamp) : 0;
                    }
                    return null;
                }
        );

        try {
            addLightSource.invoke(null, proxy);
            ACTIVE.put(id, proxy);
        } catch (Throwable t) {
            BLMod.LOGGER.debug("Failed to register DynamicLights source for {}", id, t);
        }
    }

    public static void removeFor(ServerPlayerEntity player) {
        if (!PRESENT || player == null) return;
        Object proxy = ACTIVE.remove(player.getUuid());
        if (proxy == null) return;
        try {
            removeLightSource.invoke(null, proxy);
        } catch (Throwable t) {
            BLMod.LOGGER.debug("Failed to remove DynamicLights source for {}", player.getUuid(), t);
        }
    }
}

