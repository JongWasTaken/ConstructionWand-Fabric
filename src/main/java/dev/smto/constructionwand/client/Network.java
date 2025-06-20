package dev.smto.constructionwand.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Set;
public class Network {
    @Environment(EnvType.CLIENT)
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(dev.smto.constructionwand.Network.Payloads.S2CUndoBlocksPayload.ID, (payload, context) -> {
            context.client().execute(() -> RenderBlockPreview.undoBlocks = Set.copyOf(payload.blockPosList()));
        });

        ClientPlayNetworking.registerGlobalReceiver(dev.smto.constructionwand.Network.Payloads.S2CSyncModConfigPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                dev.smto.constructionwand.Network.Payloads.S2CSyncModConfigPayload.apply(payload);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(dev.smto.constructionwand.Network.Payloads.S2CPing.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.responseSender().sendPacket(new dev.smto.constructionwand.Network.Payloads.C2SPong(true));
            });
        });
    }
}
