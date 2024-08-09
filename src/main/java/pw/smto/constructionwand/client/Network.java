package pw.smto.constructionwand.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;

import pw.smto.constructionwand.Network.*;

public class Network {
    @Environment(EnvType.CLIENT)
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(Channels.S2CHandshake.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientPlayNetworking.send(new Channels.S2CHandshakeCallback(true));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Channels.UndoBlocksPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                RenderBlockPreview.undoBlocks = Set.copyOf(payload.blockPosList());
            });
        });
    }

    public static void sendPacket(CustomPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
