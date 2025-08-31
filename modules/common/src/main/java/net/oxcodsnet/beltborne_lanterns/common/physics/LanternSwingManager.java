package net.oxcodsnet.beltborne_lanterns.common.physics;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry + updater for per-player lantern sway physics (platform-agnostic).
 * Keeps minimal kinematics to build forcing and impulses.
 */
public final class LanternSwingManager {
    // Defaults chosen to feel lively but not jittery
    private static final float DEFAULT_OMEGA = 7.0f; // rad/s (~1.1 Hz)
    private static final float DEFAULT_ZETA  = 0.42f; // damping ratio

    // Coupling gains → forcing (rad/s^2)
    private static final float K_YAW = 3.2f;         // yaw rate -> lateral sway (Z axis)
    private static final float K_HSPEED = 0.20f;     // small forward lean with speed
    private static final float K_HACCEL = 1.7f;      // forward accel -> fore-aft sway (X axis)
    private static final float K_YACCEL = 0.30f;     // vertical accel -> small extra fore-aft
    private static final float K_STRAFE_A = 1.4f;    // strafe accel -> lateral sway
    private static final float K_STRAFE_V = 0.25f;   // strafe speed -> constant slight lean

    // Impulses (rad/s)
    private static final float IMPULSE_JUMP_X = -1.3f; // tilt backwards on jump
    private static final float IMPULSE_LAND_X = +1.8f; // kick forwards on landing
    private static final float IMPULSE_CROUCH_X = +0.6f; // small forward dip on crouch
    private static final float IMPULSE_UNCROUCH_X = -0.4f; // small recovery on release
    private static final float IMPULSE_START_MOVE_X = -0.7f; // when starting to move: slight back
    private static final float IMPULSE_STOP_MOVE_X  = +0.5f; // when stopping: slight forward

    // Event detection thresholds
    private static final double MOVE_THRESHOLD_BPS = 1.6;   // blocks/sec, for start/stop impulses
    private static final double JUMP_THRESHOLD_B_TICK = 0.08; // blocks/tick, vertical
    private static final double LAND_THRESHOLD_B_TICK = -0.08;  // blocks/tick, vertical

    // Pose targets
    private static final float SNEAK_BASE_ROT_X_DEG = 215f;
    private static final float SNEAK_BLEND_TAU_SEC = 0.15f;

    // Per-player state
    private static final Map<UUID, LanternSwingState> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, Kinematics> KIN = new ConcurrentHashMap<>();

    private LanternSwingManager() {}

    public static LanternSwingState getOrCreate(UUID id) {
        return STATES.computeIfAbsent(id, u -> new LanternSwingState(DEFAULT_OMEGA, DEFAULT_ZETA));
    }

    /** Call once per client tick (20Hz). dtSec should be 1/20. */
    public static void tickPlayer(PlayerEntity p, float dtSec, float baseRotXDeg) {
        final UUID id = p.getUuid();
        final LanternSwingState state = getOrCreate(id);
        final Kinematics kin = KIN.computeIfAbsent(id, u -> new Kinematics());

        // Measure
        float yaw = wrapDegrees(p.getYaw());
        float yawRateDegPerSec = wrapDegrees(yaw - kin.prevYaw) / Math.max(dtSec, 1e-4f);
        float yawRateRadPerSec = (float) Math.toRadians(yawRateDegPerSec);

        // Velocity components
        double vx = p.getVelocity().x; // blocks/tick
        double vz = p.getVelocity().z;
        double vy = p.getVelocity().y;

        // Convert to per-second scales where needed
        double hSpeedPerSec = Math.hypot(vx, vz) * 20.0;
        double prevHSpeedPerSec = kin.prevHSpeedPerSec;
        double hAccel = (hSpeedPerSec - prevHSpeedPerSec) / Math.max(dtSec, 1e-4);

        // Vertical acceleration (approx)
        double yAcc = (vy - kin.prevYVel) * 20.0 / Math.max(dtSec, 1e-4);

        // Local frame (relative to player yaw) for forward/strafe components
        float yawRad = (float) Math.toRadians(yaw);
        double fwdX = -Math.sin(yawRad); // forward unit vector (x)
        double fwdZ =  Math.cos(yawRad); // forward unit vector (z)
        double rightX =  Math.cos(yawRad); // right unit vector (x)
        double rightZ =  Math.sin(yawRad); // right unit vector (z)

        double vFwd = vx * fwdX + vz * fwdZ;      // blocks/tick
        double vRight = vx * rightX + vz * rightZ; // blocks/tick
        double vFwdPerSec = vFwd * 20.0;
        double vRightPerSec = vRight * 20.0;
        double aFwd = (vFwdPerSec - kin.prevVFwdPerSec) / Math.max(dtSec, 1e-4);
        double aRight = (vRightPerSec - kin.prevVRightPerSec) / Math.max(dtSec, 1e-4);

        // Build forcing
        float uZ = (float) (K_YAW * yawRateRadPerSec + K_STRAFE_A * aRight + K_STRAFE_V * vRightPerSec);
        float uX = (float) (K_HSPEED * vFwdPerSec + K_HACCEL * aFwd + K_YACCEL * yAcc);

        // Impulses: jump/land/crouch transitions
        boolean onGround = p.isOnGround();
        boolean wasOnGround = kin.prevOnGround;
        boolean sneaking = p.isSneaking();
        boolean wasSneaking = kin.prevSneaking;
        boolean moving = hSpeedPerSec > MOVE_THRESHOLD_BPS;
        boolean wasMoving = kin.prevMoving;

        if (wasOnGround && !onGround && vy > JUMP_THRESHOLD_B_TICK) {
            // Jumped
            state.impulseX(IMPULSE_JUMP_X);
        }
        if (!wasOnGround && onGround && kin.prevYVel < LAND_THRESHOLD_B_TICK) {
            // Landed
            state.impulseX(IMPULSE_LAND_X);
        }
        if (!wasSneaking && sneaking) {
            state.impulseX(IMPULSE_CROUCH_X);
        } else if (wasSneaking && !sneaking) {
            state.impulseX(IMPULSE_UNCROUCH_X);
        }
        if (!wasMoving && moving) {
            state.impulseX(IMPULSE_START_MOVE_X);
        } else if (wasMoving && !moving) {
            state.impulseX(IMPULSE_STOP_MOVE_X);
        }

        // Smooth target base X (sneak pose) — approach towards SNEAK_BASE_ROT_X_DEG or provided baseRotXDeg
        float targetBaseX = sneaking ? SNEAK_BASE_ROT_X_DEG : baseRotXDeg;
        if (!kin.baseInit) {
            kin.baseXDegSmoothed = targetBaseX;
            kin.baseInit = true;
        }
        kin.baseXDegSmoothed = approachExp(kin.baseXDegSmoothed, targetBaseX, dtSec, SNEAK_BLEND_TAU_SEC);

        // Integrate
        state.step(dtSec, uX, uZ);

        // Update kinematics memory
        kin.prevYaw = yaw;
        kin.prevHSpeedPerSec = hSpeedPerSec;
        kin.prevYVel = vy;
        kin.prevOnGround = onGround;
        kin.prevSneaking = sneaking;
        kin.prevVFwdPerSec = vFwdPerSec;
        kin.prevVRightPerSec = vRightPerSec;
        kin.prevMoving = moving;

        KIN.put(id, kin);
        STATES.put(id, state);
    }

    public static float getXDeg(UUID id) {
        return STATES.getOrDefault(id, new LanternSwingState(DEFAULT_OMEGA, DEFAULT_ZETA)).getXDeg();
    }

    public static float getZDeg(UUID id) {
        return STATES.getOrDefault(id, new LanternSwingState(DEFAULT_OMEGA, DEFAULT_ZETA)).getZDeg();
    }

    public static float getBaseXDeg(UUID id) {
        Kinematics k = KIN.get(id);
        if (k == null) return 0f;
        return k.baseXDegSmoothed;
    }

    public static void removePlayer(UUID id) {
        STATES.remove(id);
        KIN.remove(id);
    }

    private static float wrapDegrees(float deg) {
        return MathHelper.wrapDegrees(deg);
    }

    private static float approachExp(float current, float target, float dtSec, float tauSec) {
        if (tauSec <= 0f) return target;
        float a = 1f - (float) Math.exp(-dtSec / tauSec);
        if (a < 0f) a = 0f; else if (a > 1f) a = 1f;
        return current + (target - current) * a;
    }

    private static final class Kinematics {
        float prevYaw = 0f;              // deg
        double prevHSpeedPerSec = 0.0;   // blocks/s
        double prevYVel = 0.0;           // blocks/tick
        boolean prevOnGround = false;
        boolean prevSneaking = false;
        double prevVFwdPerSec = 0.0;
        double prevVRightPerSec = 0.0;
        boolean prevMoving = false;
        float baseXDegSmoothed = 0f;     // smoothed base X for crouch blending
        boolean baseInit = false;
    }
}

