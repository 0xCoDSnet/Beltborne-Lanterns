package net.oxcodsnet.beltborne_lanterns.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.oxcodsnet.beltborne_lanterns.datagen.BLLanguage;

public class BLLanguageProvider extends FabricLanguageProvider {
    private final String code;

    public BLLanguageProvider(FabricDataOutput output, String code) {
        super(output, code);
        this.code = code;
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {
        BLLanguage.fill(this.code, builder::add);
    }
}
