package net.oxcodsnet.beltborne_lanterns.common.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;

public record ToggleLanternPayload() implements CustomPayload {
    public static final Id<ToggleLanternPayload> ID = new Id<>(Identifier.of(BLMod.MOD_ID, "toggle_lantern"));
    public static final PacketCodec<RegistryByteBuf, ToggleLanternPayload> CODEC = PacketCodec.unit(new ToggleLanternPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
