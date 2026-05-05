package dev.smto.constructionwand.items.core;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.wand.action.ActionAngel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class AngelCoreItem extends CoreItem {
    public AngelCoreItem(ResourceKey<Item> id) {
        super(id, new Item.Properties().stacksTo(1));
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
