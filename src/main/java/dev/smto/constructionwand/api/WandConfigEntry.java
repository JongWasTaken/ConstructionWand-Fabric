package dev.smto.constructionwand.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;

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

    public static PacketByteBuf encode(PacketByteBuf buf, WandConfigEntry w) {
        buf.writeBoolean(w.upgradeable());
        buf.writeInt(w.durability());
        buf.writeInt(w.range());
        buf.writeInt(w.angelDistance());
        buf.writeInt(w.destructionLimit());
        return buf;
    }

    public static WandConfigEntry decode(PacketByteBuf p) {
        var upgradable = p.readBoolean();
        var durability = p.readInt();
        var range = p.readInt();
        var angelDistance = p.readInt();
        var destructionLimit = p.readInt();
        return new WandConfigEntry(upgradable, durability, range, angelDistance, destructionLimit);
    }
}
