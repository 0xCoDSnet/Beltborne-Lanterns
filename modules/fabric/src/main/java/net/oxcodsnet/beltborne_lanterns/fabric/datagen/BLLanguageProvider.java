package net.oxcodsnet.beltborne_lanterns.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.oxcodsnet.beltborne_lanterns.datagen.BLLanguage;

import java.util.concurrent.CompletableFuture;

public class BLLanguageProvider extends FabricLanguageProvider {
    private final String code;

    public BLLanguageProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registries,
            String code
    ) {
        super(output, code, registries);
        this.code = code;
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registries, TranslationBuilder builder) {
        BLLanguage.fill(this.code, builder::add);
    }
}
