package pw.smto.constructionwand.integrations;

import net.fabricmc.loader.api.FabricLoader;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.containers.ContainerManager;
import pw.smto.constructionwand.integrations.container.HandlerBankStorage;
import pw.smto.constructionwand.integrations.container.HandlerBotania;
import pw.smto.constructionwand.integrations.container.HandlerPackedUpBackpack;
import pw.smto.constructionwand.integrations.container.HandlerSophisticatedBackpack;

public class ModCompat {
    private static boolean checked = false;
    public static boolean CREATE = false;
    public static boolean BANK_STORAGE = false;
    public static boolean PACKED_UP = false;
    public static boolean SOPHISTICATED_BACKPACKS = false;
    public static boolean BOTANIA = false;

    public static void checkForMods() {
        if (checked) return;

        CREATE = FabricLoader.getInstance().isModLoaded("create");
        BANK_STORAGE = FabricLoader.getInstance().isModLoaded("bankstorage");
        PACKED_UP = FabricLoader.getInstance().isModLoaded("packedup");
        SOPHISTICATED_BACKPACKS = FabricLoader.getInstance().isModLoaded("sophisticatedbackpacks");
        BOTANIA = FabricLoader.getInstance().isModLoaded("botania");

        checked = true;
    }

    public static void registerModContainerHandlers() {
        if (!checked) return;
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
    }
}
