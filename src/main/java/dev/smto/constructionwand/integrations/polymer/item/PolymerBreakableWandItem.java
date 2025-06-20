package dev.smto.constructionwand.integrations.polymer.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKey;

public class PolymerBreakableWandItem extends PolymerWandItem
{
    public PolymerBreakableWandItem(RegistryKey<Item> id, Settings properties, ToolMaterial tier) {
        super(id, properties.maxDamage(tier.durability()).repairable(tier.repairItems()));
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }
}
