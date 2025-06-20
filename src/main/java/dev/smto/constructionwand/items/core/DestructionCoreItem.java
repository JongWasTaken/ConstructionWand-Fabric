package dev.smto.constructionwand.items.core;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.wand.action.ActionDestruction;

public class DestructionCoreItem extends CoreItem
{
    public DestructionCoreItem(RegistryKey<Item> id) {
        super(id, new Item.Settings().maxCount(1));
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
