package pw.smto.constructionwand.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.packet.CustomPayload;

import java.util.Set;

import pw.smto.constructionwand.Network.*;

public class Network {
    @Environment(EnvType.CLIENT)
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(Payloads.S2CHandshake.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientPlayNetworking.send(new Payloads.C2SHandshakeCallback(true));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Payloads.S2CUndoBlocksPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                RenderBlockPreview.undoBlocks = Set.copyOf(payload.blockPosList());
            });
        });
    }

    public static void sendPacket(CustomPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
