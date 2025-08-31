package net.oxcodsnet.beltborne_lanterns.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class BLDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider((FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) ->
                        new BLLanguageProvider(output, registries, "en_us")
        );
        pack.addProvider((FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) ->
                        new BLLanguageProvider(output, registries, "ru_ru")
        );
        pack.addProvider((FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) ->
                        new BLLanguageProvider(output, registries, "es_es")
        );
        pack.addProvider((FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) ->
                        new BLLanguageProvider(output, registries, "fr_fr")
        );
        pack.addProvider((FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) ->
                        new BLLanguageProvider(output, registries, "de_de")
        );
        pack.addProvider((FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) ->
                        new BLLanguageProvider(output, registries, "zh_cn")
        );
        pack.addProvider((FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries) ->
                        new BLLanguageProvider(output, registries, "uk_ua")
        );
    }
}
