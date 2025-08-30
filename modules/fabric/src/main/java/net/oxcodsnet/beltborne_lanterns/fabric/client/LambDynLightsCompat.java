package net.oxcodsnet.beltborne_lanterns.fabric.client;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Handles optional integration with the LambDynamicLights API.
 * Uses reflection so the mod remains fully optional at compile time.
 */
final class LambDynLightsCompat {
    private LambDynLightsCompat() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void init() {
        try {
            Class<?> handlerClass = Class.forName("dev.lambdaurora.lambdynlights.api.DynamicLightHandler");
            Class<?> handlersClass = Class.forName("dev.lambdaurora.lambdynlights.api.DynamicLightHandlers");

            Function<PlayerEntity, Integer> luminance = player ->
                    BLFabricClient.clientHasLantern(player) ? 15 : 0;
            Function<PlayerEntity, Boolean> waterSensitive = player -> false;

            Method makeHandler = handlerClass.getMethod("makeHandler", Function.class, Function.class);
            Object handler = makeHandler.invoke(null, luminance, waterSensitive);

            Method makeLiving = handlerClass.getMethod("makeLivingEntityHandler", handlerClass);
            Object livingHandler = makeLiving.invoke(null, handler);

            Method register = handlersClass.getMethod("registerDynamicLightHandler", EntityType.class, handlerClass);
            register.invoke(null, EntityType.PLAYER, livingHandler);
        } catch (ReflectiveOperationException ignored) {
            // If the API isn't present or changes, simply skip integration.
        }
    }
}
