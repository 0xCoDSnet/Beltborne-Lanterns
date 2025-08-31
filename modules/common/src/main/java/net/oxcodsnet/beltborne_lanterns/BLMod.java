package net.oxcodsnet.beltborne_lanterns;

public final class BLMod {
    public static final String MOD_ID = "beltborne_lanterns";

    public static void init() {
        //
        // TODO:
        // Изменить бинд "снять/надеть" - с "shift+ПКМ" на "B"
        // Нужно добавить поддержку не только обычной лампы, но и лампы душ + добавть возможность поддержки других ламп (для будущего)
        // Проведи рефакторинг LambDynLightsCompat:
        //      1. Проверь на правильность
        //      2. Константы и прочее, вынести в конфиг
        // Когда игрок умирает:
        //      При gamerule keepInventory false - сделать так, чтобы лампа выпадала
        //      При gamerule keepInventory true - сделать так, чтобы лампа не выпадала
        // Добавить перевод конфиг файла для всех языков в BLLanguage

    }
}
