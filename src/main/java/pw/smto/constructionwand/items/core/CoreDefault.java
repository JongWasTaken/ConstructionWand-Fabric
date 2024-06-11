package pw.smto.constructionwand.items.core;

import net.minecraft.util.Identifier;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandAction;
import pw.smto.constructionwand.api.IWandCore;
import pw.smto.constructionwand.wand.action.ActionConstruction;

public class CoreDefault implements IWandCore
{
    @Override
    public int getColor() {
        return -1;
    }

    @Override
    public IWandAction getWandAction() {
        return new ActionConstruction();
    }

    @Override
    public Identifier getRegistryName() {
        return ConstructionWand.loc("default");
    }
}
