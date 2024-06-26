package pw.smto.constructionwand.containers;

import net.fabricmc.loader.api.FabricLoader;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.containers.handlers.*;

public class ContainerRegistrar
{
    public static void register() {
        ConstructionWand.containerManager.register(new HandlerCapability());
        ConstructionWand.containerManager.register(new HandlerShulkerbox());
        ConstructionWand.containerManager.register(new HandlerBundle());

        if(FabricLoader.getInstance().isModLoaded("bankstorage")) {
            ConstructionWand.containerManager.register(new HandlerBankStorage());
            ConstructionWand.LOGGER.info("Bank Storage integration added");
        }

        if(FabricLoader.getInstance().isModLoaded("packedup")) {
            ConstructionWand.containerManager.register(new HandlerPackedUpBackpack());
            ConstructionWand.LOGGER.info("Packed Up integration added");
        }

        if(FabricLoader.getInstance().isModLoaded("sophisticatedbackpacks")) {
            ConstructionWand.containerManager.register(new HandlerSophisticatedBackpack());
            ConstructionWand.LOGGER.info("Sophisticated Backpacks integration added");
        }

        if(FabricLoader.getInstance().isModLoaded("botania")) {
            ConstructionWand.containerManager.register(new HandlerBotania());
            ConstructionWand.LOGGER.info("Botania integration added");
        }
    }
}
