package net.oxcodsnet.beltborne_lanterns.fabric.compat;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import net.oxcodsnet.beltborne_lanterns.BLMod;

/**
 * LambDynamicLights entrypoint for registering the Beltborne lantern handler.
 */
public final class LambDynLightsInitializer implements DynamicLightsInitializer {
    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        LambDynLightsCompat.register(context);
    }

    @Override
    public void onInitializeDynamicLights() {
        BLMod.LOGGER.warn("Dynamic lights: LambDynamicLights v4+ API is required for Beltborne Lanterns integration.");
        LambDynLightsCompat.registerLegacyHandler();
    }
}