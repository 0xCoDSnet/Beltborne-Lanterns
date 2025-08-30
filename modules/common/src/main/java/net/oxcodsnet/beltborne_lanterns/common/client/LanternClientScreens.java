package net.oxcodsnet.beltborne_lanterns.common.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screen.Screen;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfigAccess;

/**
 * Common screen factories used by loaders.
 */
public final class LanternClientScreens {
    private LanternClientScreens() {}

    public static Screen openConfig(Screen parent) {
        // Ensure AutoConfig is initialized before opening the screen
        BLClientConfigAccess.get();
        return AutoConfig.getConfigScreen(BLClientConfig.class, parent).get();
    }
}

