package dev.smto.constructionwand.integrations.polymer.item;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;

public class PolymerInfinityWandItem extends PolymerWandItem
{
    public PolymerInfinityWandItem(RegistryKey<Item> id) {
        super(id, new Settings().maxCount(1).fireproof());
    }
}
