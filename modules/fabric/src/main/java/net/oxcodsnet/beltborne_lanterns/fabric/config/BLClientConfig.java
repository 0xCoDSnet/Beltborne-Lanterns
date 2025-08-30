package net.oxcodsnet.beltborne_lanterns.fabric.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.oxcodsnet.beltborne_lanterns.ExampleMod;

@Config(name = ExampleMod.MOD_ID)
public class BLClientConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int offsetX100 = 0; // 0

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int offsetY100 = 90; // 90

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int offsetZ100 = -37; // -37

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int pivotX100 = 0; // local pivot X

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int pivotY100 = 0; // local pivot Y

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -200, max = 200)
    public int pivotZ100 = 0; // local pivot Z

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -360, max = 360)
    public int rotXDeg = 0; //

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -360, max = 360)
    public int rotYDeg = 0;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -360, max = 360)
    public int rotZDeg = 0;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 25, max = 100)
    public int scale100 = 100; // 1

    public float fOffsetX() { return offsetX100 / 100f; }
    public float fOffsetY() { return offsetY100 / 100f; }
    public float fOffsetZ() { return offsetZ100 / 100f; }
    public float fPivotX()  { return pivotX100 / 100f; }
    public float fPivotY()  { return pivotY100 / 100f; }
    public float fPivotZ()  { return pivotZ100 / 100f; }
    public float fScale()   { return scale100 / 100f; }
}
