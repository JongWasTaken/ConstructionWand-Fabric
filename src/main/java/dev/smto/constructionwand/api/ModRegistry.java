package dev.smto.constructionwand.api;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.crafting.RecipeWandUpgrade;
import dev.smto.constructionwand.items.core.CoreItem;
import dev.smto.constructionwand.items.wand.WandItem;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;

public abstract class ModRegistry {
    public static ResourceKey<Item> createKey(String name) {
        return ResourceKey.create(Registries.ITEM, ConstructionWand.id(name));
    }

    public List<WandItem> getWands() {
        return List.of(this.getStoneWand(), this.getIronWand(), this.getDiamondWand(), this.getInfinityWand());
    }

    public List<CoreItem> getCores() {
        return List.of(this.getAngelCore(), this.getDestructionCore());
    }

    public abstract WandItem getStoneWand();
    public abstract WandItem getIronWand();
    public abstract WandItem getDiamondWand();
    public abstract WandItem getInfinityWand();
    public abstract CoreItem getAngelCore();
    public abstract CoreItem getDestructionCore();

    public abstract Identifier getUseWandStat();
    public abstract RecipeSerializer<RecipeWandUpgrade> getRecipeSerializer();

    public abstract void registerAll();
}
