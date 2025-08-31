package net.oxcodsnet.beltborne_lanterns;

import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;

public final class BLMod {
    public static final String MOD_ID = "beltborne_lanterns";

    public static void init() {
        LampRegistry.init();

        // TODO: Проведи рефакторинг LambDynLightsCompat:
        //      1. Проверь на правильность
        //      2. Константы и прочее, вынести в конфиг
        // TODO: Когда игрок умирает:
        //      При gamerule keepInventory false - сделать так, чтобы лампа выпадала
        //      При gamerule keepInventory true - сделать так, чтобы лампа не выпадала
        // TODO: Добавить перевод конфиг файла для всех языков в BLLanguage

    }
}
