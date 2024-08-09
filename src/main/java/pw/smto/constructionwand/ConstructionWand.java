package pw.smto.constructionwand;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pw.smto.constructionwand.basics.ConfigClient;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.basics.ReplacementRegistry;
import pw.smto.constructionwand.containers.ContainerManager;
import pw.smto.constructionwand.integrations.ModCompat;
import pw.smto.constructionwand.wand.undo.PlayerInstance;

public class ConstructionWand implements ModInitializer
{
    public static final String MOD_ID = "constructionwand";
    public static final String MODNAME = "ConstructionWand";

    public static final Logger LOGGER = LogManager.getLogger();

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("ConstructionWand says hello - may the odds be ever in your favor.");
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        Registry.registerAll();
        ConfigServer.init();
        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ConfigServer.SPEC);
        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, ConfigClient.SPEC);
        Network.init();
        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            ReplacementRegistry.init();
            ModCompat.checkForMods();
            ContainerManager.init();
        });
        ServerPlayConnectionEvents.JOIN.register((ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) -> {
            sender.sendPacket(new Network.Channels.S2CHandshake(true));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
            PlayerInstance.removePlayerEntity(handler.player);
        });
    }
}
