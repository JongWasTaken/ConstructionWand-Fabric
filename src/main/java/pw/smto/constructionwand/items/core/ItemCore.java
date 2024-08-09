package pw.smto.constructionwand.items.core;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandCore;

import java.util.List;

public abstract class ItemCore extends Item implements IWandCore, PolymerItem
{
    public final Identifier identifier;
    private final PolymerModelData model;

    public ItemCore(Item.Settings properties, Identifier id) {
        super(properties);
        this.identifier = id;
        this.model = PolymerResourcePackUtils.requestModel(Items.GUNPOWDER, Identifier.of(ConstructionWand.MOD_ID, "item/" + id.getPath()));

    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        //if (UndoHistory.getEntryFromPlayerEntity(player).sendPackets) {
        //    return this;
        //}
        return this.model.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.value();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(
                Text.translatable(ConstructionWand.MOD_ID + ".option.cores." + getRegistryName().toString() + ".desc")
                        .formatted(Formatting.GRAY)
        );
        tooltip.add(
                Text.translatable(ConstructionWand.MOD_ID + ".tooltip.core_tip").formatted(Formatting.AQUA)
        );
    }
}
