package net.oxcodsnet.beltborne_lanterns.common.config;

/**
 * Platform-agnostic config snapshot for rendering and pose.
 * Values mirror the fabric UI config but live in common.
 */
public final class BLConfig {
    public int offsetX100 = -250;
    public int offsetY100 = -50;
    public int offsetZ100 = -600;

    public int pivotX100 = 500;
    public int pivotY100 = 600;
    public int pivotZ100 = 500;

    public int rotXDeg = 180;
    public int rotYDeg = 0;
    public int rotZDeg = 0;

    public int scale100 = 500;

    public float fOffsetX() { return offsetX100 / 1000f; }
    public float fOffsetY() { return offsetY100 / 1000f; }
    public float fOffsetZ() { return offsetZ100 / 1000f; }
    public float fPivotX()  { return pivotX100 / 1000f; }
    public float fPivotY()  { return pivotY100 / 1000f; }
    public float fPivotZ()  { return pivotZ100 / 1000f; }
    public float fScale()   { return scale100 / 1000f; }
}

