package dev.smto.constructionwand.items.core;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.wand.action.ActionAngel;

public class AngelCoreItem extends CoreItem
{
    public AngelCoreItem(RegistryKey<Item> id) {
        super(id, new Item.Settings().maxCount(1));
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
