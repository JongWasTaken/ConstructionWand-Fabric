package dev.smto.constructionwand.items.wand;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class BreakableWandItem extends WandItem
{
    private final ToolMaterial tier;
    public BreakableWandItem(Item.Settings properties, ToolMaterial tier) {
        super(properties.maxDamage(tier.getDurability()));
        this.tier = tier;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.tier.getRepairIngredient().test(ingredient);
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }
}
