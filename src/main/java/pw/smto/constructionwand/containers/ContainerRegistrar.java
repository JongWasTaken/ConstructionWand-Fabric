package pw.smto.constructionwand.containers;

import net.fabricmc.loader.api.FabricLoader;
import pw.smto.constructionwand.ConstructionWand;
//import pw.smto.constructionwand.containers.handlers.HandlerBotania;
import pw.smto.constructionwand.containers.handlers.HandlerBundle;
import pw.smto.constructionwand.containers.handlers.HandlerCapability;
import pw.smto.constructionwand.containers.handlers.HandlerShulkerbox;

public class ContainerRegistrar
{
    public static void register() {
        ConstructionWand.containerManager.register(new HandlerCapability());
        ConstructionWand.containerManager.register(new HandlerShulkerbox());
        ConstructionWand.containerManager.register(new HandlerBundle());

        // Disabled until Botania updates to 1.21
        /*
        if(FabricLoader.getInstance().isModLoaded("botania")) {
            ConstructionWand.containerManager.register(new HandlerBotania());
            ConstructionWand.LOGGER.info("Botania integration added");
        }
        */
    }
}
