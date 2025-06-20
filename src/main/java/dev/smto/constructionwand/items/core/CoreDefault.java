package dev.smto.constructionwand.items.core;

import net.minecraft.util.Identifier;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.api.IWandCore;
import dev.smto.constructionwand.wand.action.ActionConstruction;

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
        return ConstructionWand.id("default");
    }
}
