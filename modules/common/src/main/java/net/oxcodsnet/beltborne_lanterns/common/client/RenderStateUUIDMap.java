package net.oxcodsnet.beltborne_lanterns.common.client;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Weak mapping from render-state instances to player UUIDs populated via mixin at render-state update time.
 */
public final class RenderStateUUIDMap {
    private static final Map<Object, UUID> MAP = new WeakHashMap<>();

    private RenderStateUUIDMap() {}

    public static void put(Object state, UUID uuid) {
        if (state != null && uuid != null) MAP.put(state, uuid);
    }

    public static UUID get(Object state) {
        return state != null ? MAP.get(state) : null;
    }
}

