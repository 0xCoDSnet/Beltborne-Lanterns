package net.oxcodsnet.beltborne_lanterns.common.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.UUID;

public record BeltSyncPayload(UUID playerUuid, Identifier lampId) implements CustomPayload {
    public static final Id<BeltSyncPayload> ID = new Id<>(Identifier.of(BLMod.MOD_ID, "belt_sync"));
    public static final PacketCodec<RegistryByteBuf, BeltSyncPayload> CODEC = new PacketCodec<>() {
        @Override
        public BeltSyncPayload decode(RegistryByteBuf buf) {
            UUID uuid = Uuids.PACKET_CODEC.decode(buf);
            boolean has = buf.readBoolean();
            Identifier id = has ? buf.readIdentifier() : null;
            return new BeltSyncPayload(uuid, id);
        }

        @Override
        public void encode(RegistryByteBuf buf, BeltSyncPayload value) {
            Uuids.PACKET_CODEC.encode(buf, value.playerUuid());
            if (value.lampId() != null) {
                buf.writeBoolean(true);
                buf.writeIdentifier(value.lampId());
            } else {
                buf.writeBoolean(false);
            }
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
