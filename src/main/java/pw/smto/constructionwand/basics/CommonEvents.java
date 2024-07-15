package pw.smto.constructionwand.basics;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.integrations.ModCompat;

public class CommonEvents
{
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            ReplacementRegistry.init();
            ModCompat.init();
        });
        ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
            ConstructionWand.undoHistory.removePlayerEntity(handler.player);
        });
    }
}
