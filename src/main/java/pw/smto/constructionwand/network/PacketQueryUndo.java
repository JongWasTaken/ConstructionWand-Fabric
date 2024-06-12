package pw.smto.constructionwand.network;

import net.minecraft.network.PacketByteBuf;

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
