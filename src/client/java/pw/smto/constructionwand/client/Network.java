package pw.smto.constructionwand.client;

import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

import static pw.smto.constructionwand.Network.PacketData;
public class Network {
    public static void init() {
        pw.smto.constructionwand.Network.Channels.S2C_UNDO_BLOCKS.registerClientbound(pw.smto.constructionwand.Network.PacketData.UndoBlocks.class, (message, access) -> {
            if (access.runtime() == null) return;
            RenderBlockPreview.undoBlocks = Set.copyOf(message.undoBlocks());
        });
    }

    public static void sendPacket(OwoNetChannel channel) {
        channel.clientHandle().send(new PacketData.None());
    }
    public static void sendPacket(OwoNetChannel channel, Record data) {
        channel.clientHandle().send(data);
    }

}
