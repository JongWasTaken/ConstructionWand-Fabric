package dev.smto.constructionwand.items.core;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandCore;

import java.util.function.Consumer;

public abstract class CoreItem extends Item implements IWandCore
{
    public final RegistryKey<Item> registryKey;

    public CoreItem(RegistryKey<Item> id, Item.Settings properties) {
        super(properties.registryKey(id));
        this.registryKey = id;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(
                Text.translatable(ConstructionWand.MOD_ID + ".option.cores." + getRegistryName().toString() + ".desc")
                        .formatted(Formatting.GRAY)
        );
        textConsumer.accept(
                Text.translatable(ConstructionWand.MOD_ID + ".tooltip.core_tip").formatted(Formatting.AQUA)
        );
    }
}
