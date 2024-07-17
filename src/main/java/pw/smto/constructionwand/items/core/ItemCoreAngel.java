package pw.smto.constructionwand.items.core;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandAction;
import pw.smto.constructionwand.wand.action.ActionAngel;

public class ItemCoreAngel extends ItemCore
{
    public ItemCoreAngel(Item.Settings properties) {
        super(properties);
    }

    @Override
    public int getColor() {
        return 0xE9B115;
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
