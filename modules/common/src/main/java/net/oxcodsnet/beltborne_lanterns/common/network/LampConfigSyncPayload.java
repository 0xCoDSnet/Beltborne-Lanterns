package net.oxcodsnet.beltborne_lanterns.common.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Payload for syncing additional lamp luminance settings from server to client.
 */
public record LampConfigSyncPayload(Map<Identifier, Integer> lamps) implements CustomPayload {
    public static final Id<LampConfigSyncPayload> ID = new Id<>(Identifier.of(BLMod.MOD_ID, "lamp_config_sync"));

    public static final PacketCodec<RegistryByteBuf, LampConfigSyncPayload> CODEC = new PacketCodec<>() {
        @Override
        public LampConfigSyncPayload decode(RegistryByteBuf buf) {
            int size = buf.readVarInt();
            Map<Identifier, Integer> map = new LinkedHashMap<>();
            for (int i = 0; i < size; i++) {
                Identifier id = buf.readIdentifier();
                int lum = buf.readVarInt();
                map.put(id, lum);
            }
            return new LampConfigSyncPayload(map);
        }

        @Override
        public void encode(RegistryByteBuf buf, LampConfigSyncPayload value) {
            buf.writeVarInt(value.lamps().size());
            for (var entry : value.lamps().entrySet()) {
                buf.writeIdentifier(entry.getKey());
                buf.writeVarInt(entry.getValue());
            }
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}