package net.oxcodsnet.beltborne_lanterns.datagen;

import java.util.function.BiConsumer;

/**
 * Все ключи/значения локализаций лежат в common.
 * Платформенный провайдер просто вызывает fill(locale, builder::add).
 */
public final class BLLanguage {
    private BLLanguage() {
    }
    public static void fill(String code, BiConsumer<String, String> add) {
        switch (code) {
            case "en_us": {
                add.accept("...", "...");
                break;
            }
            case "ru_ru": {
                add.accept("...", "...");
                break;
            }
            case "es_es": {
                add.accept("...", "...");
                break;
            }
            case "fr_fr": {
                add.accept("...", "...");
                break;
            }
            case "de_de": {
                add.accept("...", "...");
                break;
            }
            case "zh_cn": {
                add.accept("...", "...");
                break;
            }
            case "uk_ua": {
                add.accept("...", "...");
                break;
            }
            default: {
                break;
            }
        }
    }
}
