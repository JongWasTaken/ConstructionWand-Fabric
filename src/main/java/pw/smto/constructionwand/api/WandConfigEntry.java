package pw.smto.constructionwand.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record WandConfigEntry(boolean upgradeable, int durability, int range, int angelDistance, int destructionLimit) {
    public static Codec<WandConfigEntry> CODEC = RecordCodecBuilder.create(i ->
        i.group(
            Codec.BOOL.fieldOf("upgradeable").forGetter(WandConfigEntry::upgradeable),
            Codec.INT.fieldOf("durability").forGetter(WandConfigEntry::durability),
            Codec.INT.fieldOf("range").forGetter(WandConfigEntry::range),
            Codec.INT.fieldOf("angelDistance").forGetter(WandConfigEntry::angelDistance),
            Codec.INT.fieldOf("destructionLimit").forGetter(WandConfigEntry::destructionLimit)
        ).apply(i, WandConfigEntry::new)
    );

    public static PacketCodec<ByteBuf, WandConfigEntry> PACKET_CODEC = PacketCodecs.codec(CODEC);
}
