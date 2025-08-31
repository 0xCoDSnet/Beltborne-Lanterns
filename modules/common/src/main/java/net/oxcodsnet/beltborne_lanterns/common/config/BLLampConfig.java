package net.oxcodsnet.beltborne_lanterns.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Config(name = BLMod.MOD_ID + "_lamps")
public class BLLampConfig implements ConfigData {
    /**
     * Additional lamp item ids mapped to their luminance values.
     */
    public java.util.List<BLClientConfig.ExtraLampEntry> extraLampLight = new java.util.ArrayList<>();
}
