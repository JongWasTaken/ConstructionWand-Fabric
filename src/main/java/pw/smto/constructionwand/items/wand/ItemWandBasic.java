package pw.smto.constructionwand.items.wand;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import org.jetbrains.annotations.NotNull;

public class ItemWandBasic extends ItemWand
{
    private final ToolMaterial tier;

    public ItemWandBasic(Item.Settings properties, ToolMaterial tier) {
        super(properties.maxDamage(tier.getDurability()));
        this.tier = tier;
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }

    @Override
    public boolean canRepair(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return this.tier.getRepairIngredient().test(repair);
    }
}
