package net.oxcodsnet.beltborne_lanterns.common;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;

/**
 * Handles optional integration with the LambDynamicLights API.
 * Uses reflection so the mod remains fully optional at compile time.
 */
public final class LambDynLightsCompat {
    public LambDynLightsCompat() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
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
        } catch (ReflectiveOperationException ignored) {
            try {
                Class<?> lambClass = Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");
                Method get = lambClass.getMethod("get");
                Object instance = get.invoke(null);

                Method managerMethod = lambClass.getMethod("entityLightSourceManager");
                Object manager = managerMethod.invoke(instance);

                Method eventMethod = manager.getClass().getMethod("onRegisterEvent");
                Object event = eventMethod.invoke(manager);

                Class<?> eventClass = Class.forName("dev.yumi.commons.event.Event");
                Class<?> identifierClass = Class.forName("net.minecraft.resources.Identifier");
                Method identifierOf = identifierClass.getMethod("of", String.class, String.class);
                Object identifier = identifierOf.invoke(null, "beltborne_lanterns", "player_lantern");

                Class<?> onRegisterClass = Class.forName("dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager$OnRegister");
                Class<?> registerContextClass = Class.forName("dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager$RegisterContext");
                Class<?> entityLuminanceClass = Class.forName("dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance");
                Class<?> entityLuminanceTypeClass = Class.forName("dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance$Type");
                Field valueTypeField = entityLuminanceTypeClass.getField("VALUE");
                Object valueType = valueTypeField.get(null);

                Object luminanceProxy = Proxy.newProxyInstance(
                        entityLuminanceClass.getClassLoader(),
                        new Class[]{entityLuminanceClass},
                        (proxy, method, args) -> {
                            switch (method.getName()) {
                                case "getLuminance" -> {
                                    Object entity = args[1];
                                    if (entity instanceof PlayerEntity player) {
                                        Item lamp = BLClientAbstractions.clientLamp(player);
                                        return lamp != null ? LampRegistry.getLuminance(lamp) : 0;
                                    }
                                    return 0;
                                }
                                case "type" -> {
                                    return valueType;
                                }
                                default -> {
                                    return null;
                                }
                            }
                        });

                Object listener = Proxy.newProxyInstance(
                        onRegisterClass.getClassLoader(),
                        new Class[]{onRegisterClass},
                        (proxy, method, args) -> {
                            if ("onRegister".equals(method.getName())) {
                                Object context = args[0];
                                Method registerMethod = registerContextClass.getMethod(
                                        "register", EntityType.class, entityLuminanceClass);
                                registerMethod.invoke(context, EntityType.PLAYER, luminanceProxy);
                            }
                            return null;
                        });

                Method register = eventClass.getMethod("register", Object.class, Object.class);
                register.invoke(event, identifier, listener);
            } catch (ReflectiveOperationException ignored2) {
                // If the API isn't present or changes, simply skip integration.
            }
        }
    }
}
