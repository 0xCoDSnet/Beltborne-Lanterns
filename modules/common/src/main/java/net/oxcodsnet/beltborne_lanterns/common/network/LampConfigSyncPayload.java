package net.oxcodsnet.beltborne_lanterns.common.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Payload for syncing additional lamp luminance settings from server to client.
 */
public record LampConfigSyncPayload(Map<Identifier, Integer> lamps) {
    public static final Identifier ID = new Identifier(BLMod.MOD_ID, "lamp_config_sync");

    public static LampConfigSyncPayload read(PacketByteBuf buf) {
        int size = buf.readVarInt();
        Map<Identifier, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            int luminance = buf.readVarInt();
            map.put(id, luminance);
        }
        return new LampConfigSyncPayload(map);
    }

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(lamps.size());
        for (var entry : lamps.entrySet()) {
            buf.writeIdentifier(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }
}
