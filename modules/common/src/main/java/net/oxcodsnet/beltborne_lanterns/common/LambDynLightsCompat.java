package net.oxcodsnet.beltborne_lanterns.common;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;

/**
 * Handles optional integration with the LambDynamicLights API.
 * Uses reflection so the mod remains fully optional at compile time.
 */
public final class LambDynLightsCompat {
    private static boolean INITIALIZED = false;

    public LambDynLightsCompat() {}

    public static boolean isInitialized() {
        return INITIALIZED;
    }

    // Allows platform-specific LDL4 integration to mark completion.
    public static void markInitialized() {
        INITIALIZED = true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        if (INITIALIZED) return;
        try {
            Class<?> handlerClass = Class.forName("dev.lambdaurora.lambdynlights.api.DynamicLightHandler");
            Class<?> handlersClass = Class.forName("dev.lambdaurora.lambdynlights.api.DynamicLightHandlers");

            Function<PlayerEntity, Integer> luminance = player -> {
                Item lamp = BLClientAbstractions.clientLamp(player);
                return lamp != null ? LampRegistry.getLuminance(lamp) : 0;
            };
            Function<PlayerEntity, Boolean> waterSensitive = player -> false;

            Method makeHandler = handlerClass.getMethod("makeHandler", Function.class, Function.class);
            Object handler = makeHandler.invoke(null, luminance, waterSensitive);

            Method makeLiving = handlerClass.getMethod("makeLivingEntityHandler", handlerClass);
            Object livingHandler = makeLiving.invoke(null, handler);

            Method register = handlersClass.getMethod("registerDynamicLightHandler", EntityType.class, handlerClass);
            register.invoke(null, EntityType.PLAYER, livingHandler);
            BLMod.LOGGER.info("Dynamic lights: integrated via LambDynamicLights API (handler)");
            INITIALIZED = true;
            return;
        } catch (Throwable ignored) {
            // Fallback to 4.x API below.
        }

        // LDL 4.x typed integration is provided by platform modules (compileOnly).
        // We intentionally do not attempt 4.x via reflection here to avoid duplication.
    }
}
