package net.oxcodsnet.beltborne_lanterns.common.client;

import java.util.UUID;

/**
 * Accessor implemented on PlayerEntityRenderState via mixin to store the owning player's UUID.
 */
public interface RenderStatePlayerUuidAccess {
    UUID bl$getPlayerUuid();
    void bl$setPlayerUuid(UUID uuid);
}

