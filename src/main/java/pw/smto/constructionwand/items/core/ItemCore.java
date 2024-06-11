package pw.smto.constructionwand.items.core;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandCore;

import java.util.List;

public abstract class ItemCore extends Item implements IWandCore
{
    public ItemCore(Item.Settings properties) {
        super(properties);
    }

    public void appendHoverText(@NotNull ItemStack itemstack, World worldIn, @NotNull List<Text> lines, @NotNull TooltipContext extraInfo) {
        if (!worldIn.isClient) return;
        lines.add(
                Text.translatable(ConstructionWand.MOD_ID + ".option.cores." + getRegistryName().toString() + ".desc")
                        .formatted(Formatting.GRAY)
        );
        lines.add(
                Text.translatable(ConstructionWand.MOD_ID + ".tooltip.core_tip").formatted(Formatting.AQUA)
        );
    }
}
