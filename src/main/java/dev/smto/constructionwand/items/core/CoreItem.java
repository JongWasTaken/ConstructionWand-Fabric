package dev.smto.constructionwand.items.core;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandCore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class CoreItem extends Item implements IWandCore {
    public final ResourceKey<Item> registryKey;

    protected CoreItem(ResourceKey<Item> id, Item.Properties properties) {
        super(properties.setId(id));
        this.registryKey = id;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay displayComponent, Consumer<Component> textConsumer, @NotNull TooltipFlag type) {
        textConsumer.accept(
                Component.translatable(ConstructionWand.MOD_ID + ".option.cores." + this.getRegistryName().toString() + ".desc")
                        .withStyle(ChatFormatting.GRAY)
        );
        textConsumer.accept(
                Component.translatable(ConstructionWand.MOD_ID + ".tooltip.core_tip").withStyle(ChatFormatting.AQUA)
        );
    }
}
