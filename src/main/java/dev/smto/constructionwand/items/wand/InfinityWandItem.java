package dev.smto.constructionwand.items.wand;


import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class InfinityWandItem extends WandItem
{
    public InfinityWandItem(ResourceKey<Item> id) {
        super(id, new Item.Properties().stacksTo(1).fireResistant());
    }
}
