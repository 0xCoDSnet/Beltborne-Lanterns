package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;

import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;
// import net.oxcodsnet.beltborne_lanterns.neoforge.compat.AccessoriesCompatNeoForge;

@Mod(BLMod.MOD_ID)
public final class BLNeoForge {
    public BLNeoForge() {
        String version = ModList.get()
                .getModContainerById(BLMod.MOD_ID)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("?");
        BLMod.LOGGER.info("Initializing v{} [NeoForge]", version);
        LambDynLightsCompat.init();

        // Accessories (WispForest) integration — only if the mod is present
        if (ModList.get().isLoaded("accessories")) {
            // TODO: Accessories compat moved to a separate module
            // AccessoriesCompatNeoForge.init();
        }
    }
}
