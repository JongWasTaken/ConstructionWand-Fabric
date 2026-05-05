package dev.smto.constructionwand.integrations.polymer.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class PolymerInfinityWandItem extends PolymerWandItem {
    public PolymerInfinityWandItem(ResourceKey<Item> id) {
        super(id, new Properties().stacksTo(1).fireResistant());
    }
}
