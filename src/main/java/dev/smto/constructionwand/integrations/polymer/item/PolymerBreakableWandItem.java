package dev.smto.constructionwand.integrations.polymer.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Identifier;

public class PolymerBreakableWandItem extends PolymerWandItem
{
    private final ToolMaterial tier;

    public PolymerBreakableWandItem(Identifier id, Settings properties, ToolMaterial tier) {
        super(id, properties.maxDamage(tier.getDurability()));
        this.tier = tier;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return tier.getRepairIngredient().test(ingredient);
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }
}
