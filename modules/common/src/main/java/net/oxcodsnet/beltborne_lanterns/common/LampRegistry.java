package net.oxcodsnet.beltborne_lanterns.common;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry of lamp items that can be attached to the belt and their
 * corresponding block states for rendering.
 */
public final class LampRegistry {
    private static final Map<Item, BlockState> LAMPS = new LinkedHashMap<>();

    static {
        register(Items.LANTERN, Blocks.LANTERN.getDefaultState().with(Properties.HANGING, false));
        register(Items.SOUL_LANTERN, Blocks.SOUL_LANTERN.getDefaultState().with(Properties.HANGING, false));
    }

    private LampRegistry() {}

    public static void register(Item item, BlockState state) {
        LAMPS.put(item, state);
    }

    public static boolean isLamp(ItemStack stack) {
        return stack != null && LAMPS.containsKey(stack.getItem());
    }

    public static boolean isLamp(Item item) {
        return item != null && LAMPS.containsKey(item);
    }

    public static BlockState getState(Item item) {
        return LAMPS.getOrDefault(item, Blocks.LANTERN.getDefaultState().with(Properties.HANGING, false));
    }

    public static Identifier getId(Item item) {
        return Registries.ITEM.getId(item);
    }

    public static Item getById(Identifier id) {
        return Registries.ITEM.get(id);
    }

    public static Set<Item> items() {
        return Collections.unmodifiableSet(LAMPS.keySet());
    }
}
