package dev.smto.constructionwand.items.core;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandCore;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public abstract class CoreItem extends Item implements IWandCore
{
    public final RegistryKey<Item> registryKey;

    public CoreItem(RegistryKey<Item> id, Item.Settings properties) {
        super(properties);
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
