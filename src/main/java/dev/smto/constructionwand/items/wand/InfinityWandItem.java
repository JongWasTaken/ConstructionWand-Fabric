package dev.smto.constructionwand.items.wand;


import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;

public class InfinityWandItem extends WandItem
{
    public InfinityWandItem() {
        super(new Item.Settings().maxCount(1).fireproof());
    }
}
