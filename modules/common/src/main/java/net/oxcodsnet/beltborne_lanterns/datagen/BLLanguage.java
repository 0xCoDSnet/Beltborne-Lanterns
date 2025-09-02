package net.oxcodsnet.beltborne_lanterns.datagen;

import java.util.function.BiConsumer;

/**
 * Все ключи/значения локализаций лежат в common.
 * Платформенный провайдер просто вызывает fill(locale, builder::add).
 */
public final class BLLanguage {
    private BLLanguage() {}

    public static void fill(String code, BiConsumer<String, String> add) {
        switch (code) {
            case "ru_ru":
                fillRu(add);
                break;
            case "es_es":
                fillEs(add);
                break;
            case "fr_fr":
                fillFr(add);
                break;
            case "de_de":
                fillDe(add);
                break;
            case "zh_cn":
                fillZh(add);
                break;
            case "uk_ua":
                fillUk(add);
                break;
            case "en_us":
            default:
                fillEn(add);
                break;
        }
    }

    private static void fillEn(BiConsumer<String, String> add) {
        // Keybindings
        add.accept("category.beltborne_lanterns", "Beltborne Lanterns");
        add.accept("key.beltborne_lanterns.open_config", "Open Config Screen");
        add.accept("key.beltborne_lanterns.toggle_debug", "Toggle Debug Gizmos");
        add.accept("key.beltborne_lanterns.open_debug", "Open Lantern Debug Editor");
        add.accept("key.beltborne_lanterns.toggle_lantern", "Toggle Belt Lantern");

        // AutoConfig Title
        add.accept("text.autoconfig.beltborne_lanterns.title", "Beltborne Lanterns Config");

        // AutoConfig Categories
        add.accept("text.autoconfig.beltborne_lanterns.category.default", "General Settings");
        add.accept("text.autoconfig.beltborne_lanterns.category.lamps", "Custom Lamps");

        // AutoConfig Options
        add.accept("text.autoconfig.beltborne_lanterns.option.debug", "Enable Debug");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetX100", "Offset X");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetY100", "Offset Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetZ100", "Offset Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotX100", "Pivot X");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotY100", "Pivot Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotZ100", "Pivot Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotXDeg", "Rotation X (deg)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotYDeg", "Rotation Y (deg)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotZDeg", "Rotation Z (deg)");
        add.accept("text.autoconfig.beltborne_lanterns.option.scale100", "Scale");
        add.accept("text.autoconfig.beltborne_lanterns.option.extraLampLight", "Extra Lamps");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry", "Lamp Entry");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.id", "Item ID");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.luminance", "Luminance");
    }

    private static void fillRu(BiConsumer<String, String> add) {
        // Keybindings
        add.accept("category.beltborne_lanterns", "Beltborne Lanterns");
        add.accept("key.beltborne_lanterns.open_config", "Открыть экран настроек");
        add.accept("key.beltborne_lanterns.toggle_debug", "Переключить отладочные гизмо");
        add.accept("key.beltborne_lanterns.open_debug", "Открыть редактор отладки фонаря");
        add.accept("key.beltborne_lanterns.toggle_lantern", "Переключить поясной фонарь");

        // AutoConfig Title
        add.accept("text.autoconfig.beltborne_lanterns.title", "Настройки Beltborne Lanterns");

        // AutoConfig Categories
        add.accept("text.autoconfig.beltborne_lanterns.category.default", "Общие настройки");
        add.accept("text.autoconfig.beltborne_lanterns.category.lamps", "Пользовательские лампы");

        // AutoConfig Options
        add.accept("text.autoconfig.beltborne_lanterns.option.debug", "Включить отладку");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetX100", "Смещение X");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetY100", "Смещение Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetZ100", "Смещение Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotX100", "Пивот X");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotY100", "Пивот Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotZ100", "Пивот Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotXDeg", "Вращение X (град)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotYDeg", "Вращение Y (град)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotZDeg", "Вращение Z (град)");
        add.accept("text.autoconfig.beltborne_lanterns.option.scale100", "Масштаб");
        add.accept("text.autoconfig.beltborne_lanterns.option.extraLampLight", "Дополнительные лампы");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry", "Запись о лампе");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.id", "ID Предмета");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.luminance", "Яркость");
    }

    private static void fillEs(BiConsumer<String, String> add) {
        // Keybindings
        add.accept("category.beltborne_lanterns", "Beltborne Lanterns");
        add.accept("key.beltborne_lanterns.open_config", "Abrir pantalla de configuración");
        add.accept("key.beltborne_lanterns.toggle_debug", "Alternar Gizmos de Depuración");
        add.accept("key.beltborne_lanterns.open_debug", "Abrir Editor de Depuración de Linterna");
        add.accept("key.beltborne_lanterns.toggle_lantern", "Alternar Linterna de Cinturón");

        // AutoConfig Title
        add.accept("text.autoconfig.beltborne_lanterns.title", "Configuración de Beltborne Lanterns");

        // AutoConfig Categories
        add.accept("text.autoconfig.beltborne_lanterns.category.default", "Configuraciones Generales");
        add.accept("text.autoconfig.beltborne_lanterns.category.lamps", "Lámparas Personalizadas");

        // AutoConfig Options
        add.accept("text.autoconfig.beltborne_lanterns.option.debug", "Habilitar Depuración");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetX100", "Desplazamiento X");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetY100", "Desplazamiento Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetZ100", "Desplazamiento Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotX100", "Pivote X");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotY100", "Pivote Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotZ100", "Pivote Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotXDeg", "Rotación X (grados)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotYDeg", "Rotación Y (grados)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotZDeg", "Rotación Z (grados)");
        add.accept("text.autoconfig.beltborne_lanterns.option.scale100", "Escala");
        add.accept("text.autoconfig.beltborne_lanterns.option.extraLampLight", "Lámparas Adicionales");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry", "Entrada de Lámpara");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.id", "ID del Objeto");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.luminance", "Luminancia");
    }

    private static void fillFr(BiConsumer<String, String> add) {
        // Keybindings
        add.accept("category.beltborne_lanterns", "Beltborne Lanterns");
        add.accept("key.beltborne_lanterns.open_config", "Ouvrir l'écran de configuration");
        add.accept("key.beltborne_lanterns.toggle_debug", "Activer/Désactiver les Gizmos de Débogage");
        add.accept("key.beltborne_lanterns.open_debug", "Ouvrir l'Éditeur de Débogage de Lanterne");
        add.accept("key.beltborne_lanterns.toggle_lantern", "Activer/Désactiver la Lanterne de Ceinture");

        // AutoConfig Title
        add.accept("text.autoconfig.beltborne_lanterns.title", "Configuration de Beltborne Lanterns");

        // AutoConfig Categories
        add.accept("text.autoconfig.beltborne_lanterns.category.default", "Paramètres Généraux");
        add.accept("text.autoconfig.beltborne_lanterns.category.lamps", "Lampes Personnalisées");

        // AutoConfig Options
        add.accept("text.autoconfig.beltborne_lanterns.option.debug", "Activer le Débogage");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetX100", "Décalage X");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetY100", "Décalage Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetZ100", "Décalage Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotX100", "Pivot X");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotY100", "Pivot Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotZ100", "Pivot Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotXDeg", "Rotation X (deg)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotYDeg", "Rotation Y (deg)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotZDeg", "Rotation Z (deg)");
        add.accept("text.autoconfig.beltborne_lanterns.option.scale100", "Échelle");
        add.accept("text.autoconfig.beltborne_lanterns.option.extraLampLight", "Lampes Supplémentaires");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry", "Entrée de Lampe");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.id", "ID de l'objet");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.luminance", "Luminance");
    }

    private static void fillDe(BiConsumer<String, String> add) {
        // Keybindings
        add.accept("category.beltborne_lanterns", "Beltborne Lanterns");
        add.accept("key.beltborne_lanterns.open_config", "Konfigurationsbildschirm öffnen");
        add.accept("key.beltborne_lanterns.toggle_debug", "Debug-Gizmos umschalten");
        add.accept("key.beltborne_lanterns.open_debug", "Laternen-Debug-Editor öffnen");
        add.accept("key.beltborne_lanterns.toggle_lantern", "Gürtellaterne umschalten");

        // AutoConfig Title
        add.accept("text.autoconfig.beltborne_lanterns.title", "Konfiguration für Beltborne Lanterns");

        // AutoConfig Categories
        add.accept("text.autoconfig.beltborne_lanterns.category.default", "Allgemeine Einstellungen");
        add.accept("text.autoconfig.beltborne_lanterns.category.lamps", "Benutzerdefinierte Lampen");

        // AutoConfig Options
        add.accept("text.autoconfig.beltborne_lanterns.option.debug", "Debug aktivieren");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetX100", "Versatz X");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetY100", "Versatz Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetZ100", "Versatz Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotX100", "Drehpunkt X");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotY100", "Drehpunkt Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotZ100", "Drehpunkt Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotXDeg", "Rotation X (Grad)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotYDeg", "Rotation Y (Grad)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotZDeg", "Rotation Z (Grad)");
        add.accept("text.autoconfig.beltborne_lanterns.option.scale100", "Skalierung");
        add.accept("text.autoconfig.beltborne_lanterns.option.extraLampLight", "Zusätzliche Lampen");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry", "Lampeneintrag");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.id", "Gegenstand-ID");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.luminance", "Leuchtkraft");
    }

    private static void fillZh(BiConsumer<String, String> add) {
        // Keybindings
        add.accept("category.beltborne_lanterns", "Beltborne Lanterns");
        add.accept("key.beltborne_lanterns.open_config", "打开配置屏幕");
        add.accept("key.beltborne_lanterns.toggle_debug", "切换调试 Gizmos");
        add.accept("key.beltborne_lanterns.open_debug", "打开灯笼调试编辑器");
        add.accept("key.beltborne_lanterns.toggle_lantern", "切换腰带灯笼");

        // AutoConfig Title
        add.accept("text.autoconfig.beltborne_lanterns.title", "Beltborne Lanterns 配置");

        // AutoConfig Categories
        add.accept("text.autoconfig.beltborne_lanterns.category.default", "常规设置");
        add.accept("text.autoconfig.beltborne_lanterns.category.lamps", "自定义灯笼");

        // AutoConfig Options
        add.accept("text.autoconfig.beltborne_lanterns.option.debug", "启用调试");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetX100", "偏移 X");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetY100", "偏移 Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetZ100", "偏移 Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotX100", "轴心 X");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotY100", "轴心 Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotZ100", "轴心 Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotXDeg", "旋转 X (度)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotYDeg", "旋转 Y (度)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotZDeg", "旋转 Z (度)");
        add.accept("text.autoconfig.beltborne_lanterns.option.scale100", "缩放");
        add.accept("text.autoconfig.beltborne_lanterns.option.extraLampLight", "额外灯笼");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry", "灯笼条目");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.id", "物品 ID");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.luminance", "亮度");
    }

    private static void fillUk(BiConsumer<String, String> add) {
        // Keybindings
        add.accept("category.beltborne_lanterns", "Beltborne Lanterns");
        add.accept("key.beltborne_lanterns.open_config", "Відкрити екран налаштувань");
        add.accept("key.beltborne_lanterns.toggle_debug", "Перемкнути відлагоджувальні гизмо");
        add.accept("key.beltborne_lanterns.open_debug", "Відкрити редактор відлагодження ліхтаря");
        add.accept("key.beltborne_lanterns.toggle_lantern", "Перемкнути поясний ліхтар");

        // AutoConfig Title
        add.accept("text.autoconfig.beltborne_lanterns.title", "Налаштування Beltborne Lanterns");

        // AutoConfig Categories
        add.accept("text.autoconfig.beltborne_lanterns.category.default", "Загальні налаштування");
        add.accept("text.autoconfig.beltborne_lanterns.category.lamps", "Користувацькі лампи");

        // AutoConfig Options
        add.accept("text.autoconfig.beltborne_lanterns.option.debug", "Увімкнути відлагодження");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetX100", "Зсув X");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetY100", "Зсув Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.offsetZ100", "Зсув Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotX100", "Півот X");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotY100", "Півот Y");
        add.accept("text.autoconfig.beltborne_lanterns.option.pivotZ100", "Півот Z");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotXDeg", "Обертання X (град)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotYDeg", "Обертання Y (град)");
        add.accept("text.autoconfig.beltborne_lanterns.option.rotZDeg", "Обертання Z (град)");
        add.accept("text.autoconfig.beltborne_lanterns.option.scale100", "Масштаб");
        add.accept("text.autoconfig.beltborne_lanterns.option.extraLampLight", "Додаткові лампи");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry", "Запис про лампу");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.id", "ID Предмета");
        add.accept("text.autoconfig.beltborne_lanterns.option.ExtraLampEntry.luminance", "Яскравість");
    }
}
