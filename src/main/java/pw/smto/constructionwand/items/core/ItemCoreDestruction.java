package pw.smto.constructionwand.items.core;

import net.minecraft.util.Identifier;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandAction;
import pw.smto.constructionwand.wand.action.ActionDestruction;

public class ItemCoreDestruction extends ItemCore
{
    public ItemCoreDestruction(Settings properties) {
        super(properties, ConstructionWand.id("core_destruction"));
    }

    @Override
    public int getColor() {
        return -65536;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionDestruction();
    }

    @Override
    public Identifier getRegistryName() {
        return ConstructionWand.id("core_destruction");
    }
}
