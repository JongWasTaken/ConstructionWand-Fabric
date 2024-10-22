package pw.smto.constructionwand.items.core;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandCore;

import java.util.List;

public abstract class ItemCore extends Item implements IWandCore
{
    public final RegistryKey<Item> registryKey;

    public ItemCore(RegistryKey<Item> id, Item.Settings properties) {
        super(properties.registryKey(id));
        this.registryKey = id;
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
