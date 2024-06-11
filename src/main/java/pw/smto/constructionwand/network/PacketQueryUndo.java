package pw.smto.constructionwand.network;

import net.minecraft.network.PacketByteBuf;
import pw.smto.constructionwand.ConstructionWand;

import java.util.function.Supplier;

public class PacketQueryUndo
{
    public boolean undoPressed;

    public PacketQueryUndo(boolean undoPressed) {
        this.undoPressed = undoPressed;
    }

    public static void encode(PacketQueryUndo msg, PacketByteBuf buffer) {
        buffer.writeBoolean(msg.undoPressed);
    }

    public static PacketQueryUndo decode(PacketByteBuf buffer) {
        return new PacketQueryUndo(buffer.readBoolean());
    }

}
