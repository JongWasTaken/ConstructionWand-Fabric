package dev.smto.constructionwand.items.core;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.wand.action.ActionDestruction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class DestructionCoreItem extends CoreItem {
    public DestructionCoreItem(ResourceKey<Item> id) {
        super(id, new Item.Properties().stacksTo(1));
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
