package net.oxcodsnet.beltborne_lanterns.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Config(name = BLMod.MOD_ID)
public class BLClientConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean debug = false;

    // {offset:[-0,25,-0,05,-0,60], pivot:[0,50,0,60,0,50], rot:[180,0,0], scale:0,50}
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int offsetX100 = -25; // 0

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int offsetY100 = -5; // 90

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int offsetZ100 = -60; // -37

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int pivotX100 = 50; // local pivot X

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int pivotY100 = 60; // local pivot Y

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int pivotZ100 = 50; // local pivot Z

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -360, max = 360)
    public int rotXDeg = 180; //

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -360, max = 360)
    public int rotYDeg = 0;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -360, max = 360)
    public int rotZDeg = 0;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 25, max = 100)
    public int scale100 = 50; // 1


    @ConfigEntry.Category("lamps")
    public java.util.List<ExtraLampEntry> extraLampLight = new java.util.ArrayList<>();

    public float fOffsetX() { return offsetX100 / 100f; }
    public float fOffsetY() { return offsetY100 / 100f; }
    public float fOffsetZ() { return offsetZ100 / 100f; }
    public float fPivotX()  { return pivotX100 / 100f; }
    public float fPivotY()  { return pivotY100 / 100f; }
    public float fPivotZ()  { return pivotZ100 / 100f; }
    public float fScale()   { return scale100 / 100f; }

    public static class ExtraLampEntry {
        @ConfigEntry.Gui.Tooltip
        public String id = "modid:item_id";

        @ConfigEntry.Gui.Tooltip @ConfigEntry.BoundedDiscrete(min = 0, max = 15)
        public int luminance = 15;

        public ExtraLampEntry() {}

        public ExtraLampEntry(String id, int luminance) {
            this.id = id;
            this.luminance = luminance;
        }
    }
}
