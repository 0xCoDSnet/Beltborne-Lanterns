package net.oxcodsnet.beltborne_lanterns.fabric.client.physics;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry + updater for per-player lantern sway physics on client.
 * Keeps minimal kinematics to build forcing and impulses.
 */
public final class LanternSwingManager {
    // Defaults chosen to feel lively but not jittery
    private static final float DEFAULT_OMEGA = 7.0f; // rad/s (~1.1 Hz)
    private static final float DEFAULT_ZETA  = 0.42f; // damping ratio

    // Coupling gains → forcing (rad/s^2)
    private static final float K_YAW = 2.0f;         // yaw rate -> lateral sway (Z axis)
    private static final float K_HSPEED = 0.0f;      // not used for now (could lean with speed)
    private static final float K_HACCEL = 1.3f;      // forward accel -> fore-aft sway (X axis)
    private static final float K_YACCEL = 0.25f;     // vertical accel -> small extra fore-aft

    // Impulses (rad/s)
    private static final float IMPULSE_JUMP_X = -1.2f; // tilt backwards on jump
    private static final float IMPULSE_LAND_X = +1.6f; // kick forwards on landing
    private static final float IMPULSE_CROUCH_X = +0.6f; // small forward dip on crouch
    private static final float IMPULSE_UNCROUCH_X = -0.4f; // small recovery on release

    // Per-player state
    private static final Map<UUID, LanternSwingState> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, Kinematics> KIN = new ConcurrentHashMap<>();

    private LanternSwingManager() {}

    public static LanternSwingState getOrCreate(UUID id) {
        return STATES.computeIfAbsent(id, u -> new LanternSwingState(DEFAULT_OMEGA, DEFAULT_ZETA));
    }

    /** Call once per client tick (20Hz). dtSec should be 1/20. */
    public static void tickPlayer(PlayerEntity p, float dtSec) {
        final UUID id = p.getUuid();
        final LanternSwingState state = getOrCreate(id);
        final Kinematics kin = KIN.computeIfAbsent(id, u -> new Kinematics());

        // Measure
        float yaw = wrapDegrees(p.getYaw());
        float yawRateDegPerSec = wrapDegrees(yaw - kin.prevYaw) / Math.max(dtSec, 1e-4f);
        float yawRateRadPerSec = (float) Math.toRadians(yawRateDegPerSec);

        // Horizontal speed and accel (blocks/s)
        double vx = p.getVelocity().x; // units are ~blocks/tick; multiply by 20 to get per second
        double vz = p.getVelocity().z;
        double vy = p.getVelocity().y;
        double hSpeedPerSec = Math.hypot(vx, vz) * 20.0;
        double prevHSpeedPerSec = kin.prevHSpeedPerSec;
        double hAccel = (hSpeedPerSec - prevHSpeedPerSec) / Math.max(dtSec, 1e-4);

        // Vertical acceleration (approx)
        double yAcc = (vy - kin.prevYVel) * 20.0 / Math.max(dtSec, 1e-4);

        // Build forcing
        float uZ = K_YAW * yawRateRadPerSec;
        float uX = (float) (K_HSPEED * hSpeedPerSec + K_HACCEL * hAccel + K_YACCEL * yAcc);

        // Impulses: jump/land/crouch transitions
        boolean onGround = p.isOnGround();
        boolean wasOnGround = kin.prevOnGround;
        boolean sneaking = p.isSneaking();
        boolean wasSneaking = kin.prevSneaking;

        if (wasOnGround && !onGround && vy > 0.08) {
            // Jumped
            state.impulseX(IMPULSE_JUMP_X);
        }
        if (!wasOnGround && onGround && kin.prevYVel < -0.08) {
            // Landed
            state.impulseX(IMPULSE_LAND_X);
        }
        if (!wasSneaking && sneaking) {
            state.impulseX(IMPULSE_CROUCH_X);
        } else if (wasSneaking && !sneaking) {
            state.impulseX(IMPULSE_UNCROUCH_X);
        }

        // Integrate
        state.step(dtSec, uX, uZ);

        // Update kinematics memory
        kin.prevYaw = yaw;
        kin.prevHSpeedPerSec = hSpeedPerSec;
        kin.prevYVel = vy;
        kin.prevOnGround = onGround;
        kin.prevSneaking = sneaking;

        KIN.put(id, kin);
        STATES.put(id, state);
    }

    public static float getXDeg(UUID id) {
        return STATES.getOrDefault(id, new LanternSwingState(DEFAULT_OMEGA, DEFAULT_ZETA)).getXDeg();
    }

    public static float getZDeg(UUID id) {
        return STATES.getOrDefault(id, new LanternSwingState(DEFAULT_OMEGA, DEFAULT_ZETA)).getZDeg();
    }

    private static float wrapDegrees(float deg) {
        return MathHelper.wrapDegrees(deg);
    }

    private static final class Kinematics {
        float prevYaw = 0f;              // deg
        double prevHSpeedPerSec = 0.0;   // blocks/s
        double prevYVel = 0.0;           // blocks/tick
        boolean prevOnGround = false;
        boolean prevSneaking = false;
    }
}

