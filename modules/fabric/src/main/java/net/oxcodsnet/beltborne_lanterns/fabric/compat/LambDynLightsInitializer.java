package net.oxcodsnet.beltborne_lanterns.fabric.compat;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class LambDynLightsInitializer implements DynamicLightsInitializer {
    // Required by LDL4 API until the no-arg is removed in future versions.
    @Override
    @SuppressWarnings({"deprecation", "removal"})
    public void onInitializeDynamicLights() {
        // Intentionally no-op. LDL4 calls the context overload below.
    }

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        if (context != null) {
            LambDynLightsCompat.register(context);
        }
    }
}
