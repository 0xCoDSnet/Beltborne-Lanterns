package net.oxcodsnet.beltborne_lanterns.common.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.oxcodsnet.beltborne_lanterns.ExampleMod;

import java.util.UUID;

public record BeltSyncPayload(UUID playerUuid, boolean hasLantern) implements CustomPayload {
    public static final Id<BeltSyncPayload> ID = new Id<>(Identifier.of(ExampleMod.MOD_ID, "belt_sync"));
    public static final PacketCodec<RegistryByteBuf, BeltSyncPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, BeltSyncPayload::playerUuid,
            PacketCodecs.BOOL, BeltSyncPayload::hasLantern,
            BeltSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
