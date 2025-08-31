package net.oxcodsnet.beltborne_lanterns.common;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import net.oxcodsnet.beltborne_lanterns.BLMod;

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
    private static final TagKey<Item> EXTRA_LAMPS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(BLMod.MOD_ID, "lamps"));

    private LampRegistry() {}

    /**
     * Registers vanilla lamps and any additional items provided via the
     * {@code beltborne_lanterns:lamps} item tag. The tag is intended as an
     * extension point so data packs or other mods can supply their own
     * supported lamps without code changes.
     */
    public static void init() {
        // Built-in vanilla lamps
        register(Items.LANTERN, Blocks.LANTERN.getDefaultState().with(Properties.HANGING, false));
        register(Items.SOUL_LANTERN, Blocks.SOUL_LANTERN.getDefaultState().with(Properties.HANGING, false));

        // Dynamically register any additional tagged items
        Registries.ITEM.getEntryList(EXTRA_LAMPS_TAG).ifPresent(list -> {
            for (RegistryEntry<Item> entry : list) {
                Item item = entry.value();
                if (LAMPS.containsKey(item)) continue;
                if (item instanceof BlockItem blockItem) {
                    BlockState state = blockItem.getBlock().getDefaultState();
                    if (state.contains(Properties.HANGING)) {
                        state = state.with(Properties.HANGING, false);
                    }
                    register(item, state);
                }
            }
        });
    }

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
