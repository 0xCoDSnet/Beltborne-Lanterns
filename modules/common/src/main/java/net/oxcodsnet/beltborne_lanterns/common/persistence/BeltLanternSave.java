package net.oxcodsnet.beltborne_lanterns.common.persistence;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.minecraft.world.PersistentState;

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

    private static final PersistentState.Type<BeltLanternSave> TYPE = new PersistentState.Type<>(
            BeltLanternSave::new,
            BeltLanternSave::fromNbt,
            null
    );

    public static BeltLanternSave get(MinecraftServer server) {
        var psManager = server.getOverworld().getPersistentStateManager();
        return psManager.getOrCreate(TYPE, SAVE_NAME);
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

    public static BeltLanternSave fromNbt(NbtCompound nbt) {
        BeltLanternSave save = new BeltLanternSave();
        NbtCompound map = nbt.getCompound(PLAYERS_KEY);
        for (String key : map.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                Identifier id = Identifier.tryParse(map.getString(key));
                if (id != null) {
                    save.playersWithLamps.put(uuid, id);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return save;
    }

    public static BeltLanternSave fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return fromNbt(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound map = new NbtCompound();
        for (Map.Entry<UUID, Identifier> e : playersWithLamps.entrySet()) {
            map.putString(e.getKey().toString(), e.getValue().toString());
        }
        nbt.put(PLAYERS_KEY, map);
        return nbt;
    }
}
