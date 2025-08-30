package net.oxcodsnet.beltborne_lanterns.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfigAccess;

public final class BLFabricModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // Ensure config system is initialized before opening the screen
            BLClientConfigAccess.get();
            return AutoConfig.getConfigScreen(BLClientConfig.class, parent).get();
        };
    }
}

