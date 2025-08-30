package net.oxcodsnet.beltborne_lanterns.common.config;

/**
 * Platform-agnostic config snapshot for rendering and pose.
 * Values mirror the fabric UI config but live in common.
 */
public final class BLConfig {
    public int offsetX100 = -25;
    public int offsetY100 = -5;
    public int offsetZ100 = -60;

    public int pivotX100 = 50;
    public int pivotY100 = 60;
    public int pivotZ100 = 50;

    public int rotXDeg = 180;
    public int rotYDeg = 0;
    public int rotZDeg = 0;

    public int scale100 = 50;

    public float fOffsetX() { return offsetX100 / 100f; }
    public float fOffsetY() { return offsetY100 / 100f; }
    public float fOffsetZ() { return offsetZ100 / 100f; }
    public float fPivotX()  { return pivotX100 / 100f; }
    public float fPivotY()  { return pivotY100 / 100f; }
    public float fPivotZ()  { return pivotZ100 / 100f; }
    public float fScale()   { return scale100 / 100f; }
}

