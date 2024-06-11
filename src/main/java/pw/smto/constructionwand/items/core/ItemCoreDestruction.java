package pw.smto.constructionwand.items.core;

import net.minecraft.util.Identifier;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandAction;
import pw.smto.constructionwand.wand.action.ActionDestruction;

public class ItemCoreDestruction extends ItemCore
{
    public ItemCoreDestruction(Settings properties) {
        super(properties);
    }

    @Override
    public int getColor() {
        return 0xFF0000;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionDestruction();
    }

    @Override
    public Identifier getRegistryName() {
        return ConstructionWand.loc("core_destruction");
    }
}
