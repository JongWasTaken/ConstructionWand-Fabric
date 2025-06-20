package dev.smto.constructionwand.integrations;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.integrations.polymer.PolymerManager;
import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    private static boolean checked = false;

    public static boolean create = false;
    public static boolean bankStorage = false;
    public static boolean packedUp = false;
    public static boolean sophisticatedBackpacks = false;
    public static boolean botania = false;
    public static boolean polymerEnabled = false;

    public static void checkForMods() {
        if (checked) return;

        create = FabricLoader.getInstance().isModLoaded("create");
        bankStorage = FabricLoader.getInstance().isModLoaded("bankstorage");
        packedUp = FabricLoader.getInstance().isModLoaded("packedup");
        sophisticatedBackpacks = FabricLoader.getInstance().isModLoaded("sophisticatedbackpacks");
        botania = FabricLoader.getInstance().isModLoaded("botania");
        polymerEnabled = FabricLoader.getInstance().isModLoaded(ConstructionWand.MOD_ID + "-polymer");

        if (polymerEnabled) PolymerManager.init();
        checked = true;
    }

    public static void registerModContainerHandlers() {
        if (!checked) return;
        /*
        if(BANK_STORAGE) {
            ContainerManager.register(new HandlerBankStorage());
            ConstructionWand.LOGGER.info("Bank Storage integration added");
        }

        if(PACKED_UP) {
            ContainerManager.register(new HandlerPackedUpBackpack());
            ConstructionWand.LOGGER.info("Packed Up integration added");
        }

        if(SOPHISTICATED_BACKPACKS) {
            ContainerManager.register(new HandlerSophisticatedBackpack());
            ConstructionWand.LOGGER.info("Sophisticated Backpacks integration added");
        }

        if(BOTANIA) {
            ContainerManager.register(new HandlerBotania());
            ConstructionWand.LOGGER.info("Botania integration added");
        }

         */
    }
}
