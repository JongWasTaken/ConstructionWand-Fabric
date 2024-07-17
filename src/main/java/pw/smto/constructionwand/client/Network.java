package pw.smto.constructionwand.client;

import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Set;

import static pw.smto.constructionwand.Network.PacketData;
public class Network {
    @Environment(EnvType.CLIENT)
    public static void init() {
        pw.smto.constructionwand.Network.Channels.S2C_UNDO_BLOCKS.registerClientbound(pw.smto.constructionwand.Network.PacketData.UndoBlocks.class, (message, access) -> {
            if (access.runtime() == null) return;
            RenderBlockPreview.undoBlocks = Set.copyOf(message.undoBlocks());
        });
    }

    public static void sendPacket(OwoNetChannel channel, Record data) {
        channel.clientHandle().send(data);
    }
}
