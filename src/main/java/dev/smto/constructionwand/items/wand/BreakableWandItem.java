package dev.smto.constructionwand.items.wand;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class BreakableWandItem extends WandItem {
    public BreakableWandItem(ResourceKey<Item> id, Item.Properties properties, ToolMaterial tier) {
        super(id, properties.durability(tier.durability()).repairable(tier.repairItems()));
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }
}
