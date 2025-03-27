package pw.smto.constructionwand;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.impl.core.ForgeConfigSpecAdapter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.basics.ReplacementRegistry;
import pw.smto.constructionwand.containers.ContainerManager;
import pw.smto.constructionwand.integrations.ModCompat;
import pw.smto.constructionwand.wand.undo.UndoHistory;

public class ConstructionWand implements ModInitializer
{
    public static final String MOD_ID = "constructionwand";
    public static final Logger LOGGER = LogManager.getLogger();

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("ConstructionWand says hello - may the odds be ever in your favor.");
        Registry.registerAll();
        ConfigServer.init();
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, new ForgeConfigSpecAdapter(ConfigServer.SPEC));
        Network.init();
        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            ReplacementRegistry.init();
            ModCompat.checkForMods();
            ContainerManager.init();
        });
        ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> UndoHistory.removePlayerEntity(handler.player));
    }
}
