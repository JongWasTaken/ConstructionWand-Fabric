package pw.smto.constructionwand.network;

import net.minecraft.network.PacketByteBuf;
import pw.smto.constructionwand.basics.option.IOption;

import java.util.function.Supplier;

public class PacketWandOption
{
    public final String key;
    public final String value;
    public final boolean notify;

    public PacketWandOption(IOption<?> option, boolean notify) {
        this(option.getKey(), option.getValueString(), notify);
    }

    private PacketWandOption(String key, String value, boolean notify) {
        this.key = key;
        this.value = value;
        this.notify = notify;
    }

    public static void encode(PacketWandOption msg, PacketByteBuf buffer) {
        buffer.writeString(msg.key);
        buffer.writeString(msg.value);
        buffer.writeBoolean(msg.notify);
    }

    public static PacketWandOption decode(PacketByteBuf buffer) {
        return new PacketWandOption(buffer.readString(100), buffer.readString(100), buffer.readBoolean());
    }
}
