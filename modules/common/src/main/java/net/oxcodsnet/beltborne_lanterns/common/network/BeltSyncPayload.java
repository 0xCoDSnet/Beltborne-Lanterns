package net.oxcodsnet.beltborne_lanterns.common.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.UUID;

public record BeltSyncPayload(UUID playerUuid, Identifier lampId) {
    public static final Identifier ID = new Identifier(BLMod.MOD_ID, "belt_sync");

    public static BeltSyncPayload read(PacketByteBuf buf) {
        UUID uuid = buf.readUuid();
        boolean hasLamp = buf.readBoolean();
        Identifier lamp = hasLamp ? buf.readIdentifier() : null;
        return new BeltSyncPayload(uuid, lamp);
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(playerUuid);
        if (lampId != null) {
            buf.writeBoolean(true);
            buf.writeIdentifier(lampId);
        } else {
            buf.writeBoolean(false);
        }
    }
}
