package net.oxcodsnet.beltborne_lanterns.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;

import java.util.concurrent.CompletableFuture;

public class BLLampTagProvider extends FabricTagProvider.ItemTagProvider {
    public BLLampTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
        super(output, registries);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        // Fabric API 0.128+ uses getTagBuilder; older used getOrCreateTagBuilder
        // Try to call getTagBuilder; if not available in this environment, the method will still be resolved at compile time per dependency.
        this.getTagBuilder(LampRegistry.EXTRA_LAMPS_TAG)
                .add(Registries.ITEM.getId(Items.LANTERN))
                .add(Registries.ITEM.getId(Items.SOUL_LANTERN));
    }
}
