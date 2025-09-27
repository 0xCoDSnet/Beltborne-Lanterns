package net.oxcodsnet.beltborne_lanterns.common.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;

public record ToggleLanternPayload() {
    public static final Identifier ID = new Identifier(BLMod.MOD_ID, "toggle_lantern");

    public static ToggleLanternPayload read(PacketByteBuf buf) {
        return new ToggleLanternPayload();
    }

    public void write(PacketByteBuf buf) {
        // no data
    }
}
