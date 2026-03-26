package dev.smto.constructionwand.items.core;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandCore;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public abstract class CoreItem extends Item implements IWandCore
{
    public final ResourceKey<Item> registryKey;

    public CoreItem(ResourceKey<Item> id, Item.Properties properties) {
        super(properties.setId(id));
        this.registryKey = id;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        textConsumer.accept(
                Component.translatable(ConstructionWand.MOD_ID + ".option.cores." + getRegistryName().toString() + ".desc")
                        .withStyle(ChatFormatting.GRAY)
        );
        textConsumer.accept(
                Component.translatable(ConstructionWand.MOD_ID + ".tooltip.core_tip").withStyle(ChatFormatting.AQUA)
        );
    }
}
