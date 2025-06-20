package dev.smto.constructionwand.integrations.polymer.item;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class PolymerInfinityWandItem extends PolymerWandItem
{
    public PolymerInfinityWandItem(Identifier id) {
        super(id, new Settings().maxCount(1).fireproof());
    }
}
