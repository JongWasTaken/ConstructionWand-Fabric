package dev.smto.constructionwand.integrations.polymer.item;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.wand.action.ActionAngel;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class PolymerAngelCoreItem extends PolymerCoreItem
{
    public PolymerAngelCoreItem(RegistryKey<Item> id) {
        super(id, new Settings().maxCount(1));
    }

    @Override
    public int getColor() {
        return -1461995;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionAngel();
    }

    @Override
    public Identifier getRegistryName() {
        return ConstructionWand.id("core_angel");
    }
}
