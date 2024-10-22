package pw.smto.constructionwand.items.wand;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntryList;
import org.jetbrains.annotations.NotNull;

public class ItemWandBasic extends ItemWand
{
    public ItemWandBasic(RegistryKey<Item> id, Item.Settings properties, ToolMaterial tier) {
        super(id, properties.maxDamage(tier.durability()).repairable(tier.repairItems()));
    }

    @Override
    public int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }
}
