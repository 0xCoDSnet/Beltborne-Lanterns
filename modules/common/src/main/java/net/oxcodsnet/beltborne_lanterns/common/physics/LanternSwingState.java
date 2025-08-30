package net.oxcodsnet.beltborne_lanterns.common.physics;

/**
 * Simple 2-axis spring–damper (second-order) for lantern sway.
 * State is kept in radians and integrated in seconds.
 *
 * Equation per axis: x'' + 2*zeta*omega*x' + omega^2*x = u
 * where u is an external forcing (rad/s^2), e.g. from yaw rate, speed, impulses.
 */
public final class LanternSwingState {
    // Natural frequency (rad/s) and damping ratio
    private final float omega;
    private final float zeta;

    // Angles and angular velocities (radians)
    private float x;      // around local +X (fore-aft)
    private float xDot;
    private float z;      // around local +Z (side-to-side)
    private float zDot;

    // Cached for interpolation (optional)
    private float prevX;
    private float prevZ;

    // Accumulated per-tick impulses (rad/s)
    private float impulseXdot;
    private float impulseZdot;

    public LanternSwingState(float omega, float zeta) {
        this.omega = omega;
        this.zeta = zeta;
        this.x = 0f;
        this.z = 0f;
        this.xDot = 0f;
        this.zDot = 0f;
        this.prevX = 0f;
        this.prevZ = 0f;
        this.impulseXdot = 0f;
        this.impulseZdot = 0f;
    }

    /** Apply an instantaneous velocity impulse (rad/s) to the given axis. */
    public void impulseX(float vRadPerSec) { this.impulseXdot += vRadPerSec; }
    public void impulseZ(float vRadPerSec) { this.impulseZdot += vRadPerSec; }

    /**
     * Step integration by dtSec with external forcing inputs uX, uZ (rad/s^2).
     * Uses semi-implicit (symplectic) Euler: first advance velocity, then position.
     */
    public void step(float dtSec, float uX, float uZ) {
        if (dtSec <= 0f) return;

        // Save for interpolation users
        this.prevX = this.x;
        this.prevZ = this.z;

        // Inject accumulated impulses into velocities
        if (impulseXdot != 0f) {
            this.xDot += impulseXdot;
            impulseXdot = 0f;
        }
        if (impulseZdot != 0f) {
            this.zDot += impulseZdot;
            impulseZdot = 0f;
        }

        // Damped oscillator integration per axis
        float ax = uX - 2f * zeta * omega * xDot - (omega * omega) * x;
        float az = uZ - 2f * zeta * omega * zDot - (omega * omega) * z;

        xDot += ax * dtSec;
        zDot += az * dtSec;

        x += xDot * dtSec;
        z += zDot * dtSec;
    }

    public float getXRad() { return x; }
    public float getZRad() { return z; }

    public float getXDeg() { return (float) Math.toDegrees(x); }
    public float getZDeg() { return (float) Math.toDegrees(z); }

    public float getPrevXDeg() { return (float) Math.toDegrees(prevX); }
    public float getPrevZDeg() { return (float) Math.toDegrees(prevZ); }
}

