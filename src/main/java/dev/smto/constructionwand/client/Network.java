package dev.smto.constructionwand.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import java.util.Set;
public class Network {
    @Environment(EnvType.CLIENT)
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(dev.smto.constructionwand.Network.Payloads.S2CUndoBlocksPayload.getId(), (MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {
            var payload = dev.smto.constructionwand.Network.Payloads.S2CUndoBlocksPayload.decode(buf);
            client.execute(() -> RenderBlockPreview.undoBlocks = Set.copyOf(payload.blockPosList()));
        });

        ClientPlayNetworking.registerGlobalReceiver(dev.smto.constructionwand.Network.Payloads.S2CSyncModConfigPayload.getId(), (MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {
            var payload = dev.smto.constructionwand.Network.Payloads.S2CSyncModConfigPayload.decode(buf);
            client.execute(() -> {
                dev.smto.constructionwand.Network.Payloads.S2CSyncModConfigPayload.apply(payload);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(dev.smto.constructionwand.Network.Payloads.S2CPing.getId(), (MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {
            var data = dev.smto.constructionwand.Network.Payloads.C2SPong.encode(new dev.smto.constructionwand.Network.Payloads.C2SPong(true));
            client.execute(() -> {
                responseSender.sendPacket(dev.smto.constructionwand.Network.Payloads.C2SPong.getId(), data);
            });
        });
    }
}
