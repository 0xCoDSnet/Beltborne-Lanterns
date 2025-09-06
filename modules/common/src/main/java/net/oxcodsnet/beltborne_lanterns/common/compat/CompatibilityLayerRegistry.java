package net.oxcodsnet.beltborne_lanterns.common.compat;

import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * A registry for compatibility layers.
 * This class is responsible for loading and providing access to compatibility layers.
 * It uses the Java ServiceLoader to discover and load compatibility layers at runtime.
 */
public final class CompatibilityLayerRegistry {
    private static final List<CompatibilityLayer> LOADED_LAYERS = new ArrayList<>();
    private static boolean isInitialized = false;

    private CompatibilityLayerRegistry() {}

    /**
     * Loads all available compatibility layers using the ServiceLoader.
     * This method should be called only once, during mod initialization.
     * It is safe to call this method multiple times, but it will only load the layers once.
     * @param isModLoaded A function that checks if a mod is loaded.
     *                    This is used to filter out compatibility layers for mods that are not loaded.
     */
    public static void loadLayers(java.util.function.Function<String, Boolean> isModLoaded) {
        if (isInitialized) return;

        ServiceLoader.load(CompatibilityLayer.class).forEach(layer -> {
            if (isModLoaded.apply(layer.getModId())) {
                LOADED_LAYERS.add(layer);
                BLMod.LOGGER.info("Loaded compatibility layer for mod: {}", layer.getModId());
            }
        });

        isInitialized = true;
    }

    /**
     * Initializes all loaded compatibility layers.
     * This method should be called after all layers have been loaded.
     */
    public static void initializeLayers() {
        if (!isInitialized) {
            BLMod.LOGGER.warn("Compatibility layers have not been loaded yet!");
            return;
        }
        LOADED_LAYERS.forEach(CompatibilityLayer::onInitialize);
    }

    /**
     * Gets all loaded compatibility layers.
     * @return A list of all loaded compatibility layers.
     */
    public static List<CompatibilityLayer> getLayers() {
        return LOADED_LAYERS;
    }

    /**
     * Gets a list of all loaded compatibility layers that are assignable to the given class.
     * @param type The class to check for.
     * @return A list of all loaded compatibility layers that are assignable to the given class.
     * @param <T> The type of the compatibility layer.
     */
    public static <T extends CompatibilityLayer> List<T> getLayers(Class<T> type) {
        return LOADED_LAYERS.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }
}
