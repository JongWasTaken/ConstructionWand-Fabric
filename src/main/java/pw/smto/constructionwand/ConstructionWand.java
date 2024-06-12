package pw.smto.constructionwand;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pw.smto.constructionwand.basics.CommonEvents;
import pw.smto.constructionwand.basics.ConfigClient;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.containers.ContainerManager;
import pw.smto.constructionwand.containers.ContainerRegistrar;
import pw.smto.constructionwand.wand.undo.UndoHistory;

public class ConstructionWand implements ModInitializer
{
    public static final String MOD_ID = "constructionwand";
    public static final String MODNAME = "ConstructionWand";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ContainerManager containerManager = new ContainerManager();
    public static UndoHistory undoHistory = new UndoHistory();

    public static Identifier loc(String name) {
        return new Identifier(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("ConstructionWand says hello - may the odds be ever in your favor.");
        Registry.registerAll();
        ConfigServer.init();
        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ConfigServer.SPEC);
        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, ConfigClient.SPEC);
        Network.init();
        ContainerRegistrar.register();
        CommonEvents.init();
    }
}
