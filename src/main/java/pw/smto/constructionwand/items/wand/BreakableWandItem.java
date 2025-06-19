package pw.smto.constructionwand.items.wand;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKey;

public class BreakableWandItem extends WandItem
{
    public BreakableWandItem(RegistryKey<Item> id, Item.Settings properties, ToolMaterial tier) {
        super(id, properties.maxDamage(tier.durability()).repairable(tier.repairItems()));
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }
}
