package net.oxcodsnet.beltborne_lanterns.common.persistence;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * World-persistent storage of which players have the belt lantern equipped.
 * Saved in the server world's PersistentState; shared across dimensions.
 */
public final class BeltLanternSave extends PersistentState {
    private static final String SAVE_NAME = "beltborne_lanterns_belts";
    private static final String PLAYERS_KEY = "players";

    private final Set<UUID> playersWithLantern = new HashSet<>();

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
        return playersWithLantern.contains(uuid);
    }

    public void set(UUID uuid, boolean has) {
        if (has) playersWithLantern.add(uuid); else playersWithLantern.remove(uuid);
        markDirty();
    }

    public static BeltLanternSave fromNbt(NbtCompound nbt) {
        BeltLanternSave save = new BeltLanternSave();
        NbtList list = nbt.getList(PLAYERS_KEY, NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) {
            try {
                save.playersWithLantern.add(UUID.fromString(list.getString(i)));
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
        NbtList list = new NbtList();
        for (UUID u : playersWithLantern) {
            list.add(NbtString.of(u.toString()));
        }
        nbt.put(PLAYERS_KEY, list);
        return nbt;
    }
}
