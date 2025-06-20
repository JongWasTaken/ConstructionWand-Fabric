package pw.smto.constructionwand.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import pw.smto.constructionwand.ConstructionWand;

import java.util.Set;
public class Network {
    @Environment(EnvType.CLIENT)
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(pw.smto.constructionwand.Network.Payloads.S2CUndoBlocksPayload.ID, (payload, context) -> {
            context.client().execute(() -> RenderBlockPreview.undoBlocks = Set.copyOf(payload.blockPosList()));
        });

        ClientPlayNetworking.registerGlobalReceiver(pw.smto.constructionwand.Network.Payloads.S2CSyncModConfigPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ConstructionWand.LOGGER.warn(payload.toString());
                pw.smto.constructionwand.Network.Payloads.S2CSyncModConfigPayload.apply(payload);
            });
        });
    }
}
