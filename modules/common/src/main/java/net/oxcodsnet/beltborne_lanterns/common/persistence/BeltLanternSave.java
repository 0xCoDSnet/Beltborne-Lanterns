package net.oxcodsnet.beltborne_lanterns.common.persistence;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World-persistent storage of which players have the belt lantern equipped.
 * Stores full ItemStack NBT to preserve enchantments, names, etc.
 */
public final class BeltLanternSave extends PersistentState {
    private static final String SAVE_NAME = "beltborne_lanterns_belts";
    private static final String PLAYERS_KEY = "players";

    private final Map<UUID, ItemStack> playersWithLamps = new HashMap<>();

    public static BeltLanternSave get(MinecraftServer server) {
        var psManager = server.getOverworld().getPersistentStateManager();
        return psManager.getOrCreate(BeltLanternSave::fromNbt, BeltLanternSave::new, SAVE_NAME);
    }

    public boolean has(UUID uuid) {
        return playersWithLamps.containsKey(uuid);
    }

    /**
     * Returns the item type of the stored lamp, or null.
     */
    public Item get(UUID uuid) {
        ItemStack stack = playersWithLamps.get(uuid);
        return stack != null ? stack.getItem() : null;
    }

    /**
     * Returns a copy of the stored lamp stack, or null.
     */
    public ItemStack getStack(UUID uuid) {
        ItemStack stack = playersWithLamps.get(uuid);
        return stack != null ? stack.copy() : null;
    }

    /**
     * Persists a full lamp stack (stored as a single-item copy), or clears when null.
     */
    public void set(UUID uuid, ItemStack lamp) {
        if (lamp != null && !lamp.isEmpty()) {
            playersWithLamps.put(uuid, lamp.copyWithCount(1));
        } else {
            playersWithLamps.remove(uuid);
        }
        markDirty();
    }

    /**
     * Convenience setter by item type (no NBT).
     */
    public void set(UUID uuid, Item lamp) {
        if (lamp != null) {
            playersWithLamps.put(uuid, new ItemStack(lamp));
        } else {
            playersWithLamps.remove(uuid);
        }
        markDirty();
    }

    private static BeltLanternSave fromNbt(NbtCompound nbt) {
        BeltLanternSave save = new BeltLanternSave();
        NbtCompound map = nbt.getCompound(PLAYERS_KEY);
        for (String key : map.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                NbtElement el = map.get(key);
                if (el instanceof NbtCompound compound) {
                    ItemStack stack = ItemStack.fromNbt(compound);
                    if (!stack.isEmpty()) {
                        save.playersWithLamps.put(uuid, stack);
                    }
                } else if (el instanceof NbtString) {
                    Identifier id = Identifier.tryParse(el.asString());
                    if (id != null && Registries.ITEM.containsId(id)) {
                        save.playersWithLamps.put(uuid, new ItemStack(Registries.ITEM.get(id)));
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return save;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound map = new NbtCompound();
        for (Map.Entry<UUID, ItemStack> e : playersWithLamps.entrySet()) {
            ItemStack stack = e.getValue();
            if (stack != null && !stack.isEmpty()) {
                NbtCompound encoded = new NbtCompound();
                stack.copy().writeNbt(encoded);
                map.put(e.getKey().toString(), encoded);
            }
        }
        nbt.put(PLAYERS_KEY, map);
        return nbt;
    }
}
