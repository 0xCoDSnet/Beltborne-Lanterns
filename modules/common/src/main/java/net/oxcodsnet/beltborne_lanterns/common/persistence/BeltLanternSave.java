package net.oxcodsnet.beltborne_lanterns.common.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World-persistent storage of which players have the belt lantern equipped.
 * Saved in the server world's PersistentState; shared across dimensions.
 */
public final class BeltLanternSave extends PersistentState {
    private static final String SAVE_NAME = "beltborne_lanterns_belts";
    private static final String PLAYERS_KEY = "players";

    private final Map<UUID, Identifier> playersWithLamps = new HashMap<>();

    private Map<String, Identifier> toCodecMap() {
        Map<String, Identifier> out = new HashMap<>();
        for (Map.Entry<UUID, Identifier> e : playersWithLamps.entrySet()) {
            out.put(e.getKey().toString(), e.getValue());
        }
        return out;
    }

    private static BeltLanternSave fromCodecMap(Map<String, Identifier> in) {
        BeltLanternSave save = new BeltLanternSave();
        for (Map.Entry<String, Identifier> e : in.entrySet()) {
            try {
                UUID id = UUID.fromString(e.getKey());
                if (e.getValue() != null) {
                    save.playersWithLamps.put(id, e.getValue());
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return save;
    }

    public static final Codec<BeltLanternSave> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Identifier.CODEC)
                            .fieldOf(PLAYERS_KEY)
                            .forGetter(BeltLanternSave::toCodecMap)
            ).apply(instance, map -> fromCodecMap(map))
    );

    private static final PersistentStateType<BeltLanternSave> TYPE =
            new PersistentStateType<>(SAVE_NAME, BeltLanternSave::new, CODEC, DataFixTypes.LEVEL);

    public static BeltLanternSave get(MinecraftServer server) {
        var psManager = server.getOverworld().getPersistentStateManager();
        return psManager.getOrCreate(TYPE);
    }

    public boolean has(UUID uuid) {
        return playersWithLamps.containsKey(uuid);
    }

    public Identifier getId(UUID uuid) {
        return playersWithLamps.get(uuid);
    }

    public Item get(UUID uuid) {
        Identifier id = getId(uuid);
        return id != null ? Registries.ITEM.get(id) : null;
    }

    public void set(UUID uuid, Item lamp) {
        if (lamp != null) {
            playersWithLamps.put(uuid, Registries.ITEM.getId(lamp));
        } else {
            playersWithLamps.remove(uuid);
        }
        markDirty();
    }

    // Serialization handled by CODEC above in 1.21.5+
}
