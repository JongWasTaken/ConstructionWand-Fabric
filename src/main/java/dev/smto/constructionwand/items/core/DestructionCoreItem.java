package dev.smto.constructionwand.items.core;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.wand.action.ActionDestruction;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class DestructionCoreItem extends CoreItem
{
    public DestructionCoreItem() {
        super(new Item.Settings().maxCount(1));
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
