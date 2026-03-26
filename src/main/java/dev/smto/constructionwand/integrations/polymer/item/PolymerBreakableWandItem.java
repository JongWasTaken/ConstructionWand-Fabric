package dev.smto.constructionwand.integrations.polymer.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class PolymerBreakableWandItem extends PolymerWandItem
{
    public PolymerBreakableWandItem(ResourceKey<Item> id, Properties properties, ToolMaterial tier) {
        super(id, properties.durability(tier.durability()).repairable(tier.repairItems()));
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }
}
