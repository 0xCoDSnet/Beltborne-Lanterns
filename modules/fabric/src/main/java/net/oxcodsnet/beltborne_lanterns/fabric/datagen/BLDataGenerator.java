package net.oxcodsnet.beltborne_lanterns.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

public class BLDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider((FabricDataOutput output) -> new BLLanguageProvider(output, "en_us"));
        pack.addProvider((FabricDataOutput output) -> new BLLanguageProvider(output, "ru_ru"));
        pack.addProvider((FabricDataOutput output) -> new BLLanguageProvider(output, "es_es"));
        pack.addProvider((FabricDataOutput output) -> new BLLanguageProvider(output, "fr_fr"));
        pack.addProvider((FabricDataOutput output) -> new BLLanguageProvider(output, "de_de"));
        pack.addProvider((FabricDataOutput output) -> new BLLanguageProvider(output, "zh_cn"));
        pack.addProvider((FabricDataOutput output) -> new BLLanguageProvider(output, "uk_ua"));
        pack.addProvider(BLLampTagProvider::new);
    }
}
