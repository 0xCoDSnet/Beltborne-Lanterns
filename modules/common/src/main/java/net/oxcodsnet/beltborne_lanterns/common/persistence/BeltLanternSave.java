package net.oxcodsnet.beltborne_lanterns.common.persistence;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.registry.RegistryBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.DataResult;
import com.mojang.datafixers.util.Pair;

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

    private static final PersistentStateType<BeltLanternSave> TYPE = new PersistentStateType<>(
            SAVE_NAME,
            (PersistentState.Context ctx) -> new BeltLanternSave(),
            BeltLanternSave::codec,
            (net.minecraft.datafixer.DataFixTypes) null
    );

    private static Codec<BeltLanternSave> codec(PersistentState.Context ctx) {
        return Codec.of(new Encoder<>() {
            @Override
            public <T> DataResult<T> encode(BeltLanternSave value, DynamicOps<T> ops, T prefix) {
                NbtCompound nbt = new NbtCompound();
                var world = ctx.getWorldOrThrow();
                var registries = new RegistryBuilder().createWrapperLookup(world.getRegistryManager());
                value.writeNbt(nbt, registries);
                return NbtCompound.CODEC.encodeStart(ops, nbt);
            }
        }, new Decoder<>() {
            @Override
            public <T> DataResult<Pair<BeltLanternSave, T>> decode(DynamicOps<T> ops, T input) {
                return NbtCompound.CODEC.parse(ops, input).map(nbt -> {
                    var world = ctx.getWorldOrThrow();
                    var registries = new RegistryBuilder().createWrapperLookup(world.getRegistryManager());
                    return Pair.of(BeltLanternSave.fromNbt(nbt, registries), input);
                });
            }
        });
    }

    public static BeltLanternSave get(MinecraftServer server) {
        var psManager = server.getOverworld().getPersistentStateManager();
        return (BeltLanternSave) psManager.getOrCreate(TYPE);
    }

    public BeltLanternSave() {}

    public BeltLanternSave(PersistentState.Context ctx) {}

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

    public static BeltLanternSave fromNbt(NbtCompound nbt) {
        // Fallback path (legacy) – will not decode full stacks due to missing registry lookup
        BeltLanternSave save = new BeltLanternSave();
        NbtCompound map = nbt.getCompoundOrEmpty(PLAYERS_KEY);
        for (String key : map.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                map.getString(key).ifPresent(str -> {
                    Identifier id = Identifier.tryParse(str);
                    if (id != null) {
                        save.playersWithLamps.put(uuid, new ItemStack(net.minecraft.registry.Registries.ITEM.get(id)));
                    }
                });
            } catch (IllegalArgumentException ignored) {}
        }
        return save;
    }

    public static BeltLanternSave fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        BeltLanternSave save = new BeltLanternSave();
        NbtCompound map = nbt.getCompoundOrEmpty(PLAYERS_KEY);
        for (String key : map.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                NbtElement el = map.get(key);
                if (el != null && !(el instanceof NbtString)) { // new format: full encoded stack
                    ItemStack stack = ItemStack.fromNbt(registryLookup, el).orElse(ItemStack.EMPTY);
                    if (!stack.isEmpty()) {
                        save.playersWithLamps.put(uuid, stack);
                    }
                } else {
                    map.getString(key).ifPresent(str -> {
                        Identifier id = Identifier.tryParse(str);
                        if (id != null) {
                            save.playersWithLamps.put(uuid, new ItemStack(net.minecraft.registry.Registries.ITEM.get(id)));
                        }
                    });
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return save;
    }

    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound map = new NbtCompound();
        for (Map.Entry<UUID, ItemStack> e : playersWithLamps.entrySet()) {
            NbtElement encoded = e.getValue().toNbt(registryLookup);
            map.put(e.getKey().toString(), encoded);
        }
        nbt.put(PLAYERS_KEY, map);
        return nbt;
    }
}
