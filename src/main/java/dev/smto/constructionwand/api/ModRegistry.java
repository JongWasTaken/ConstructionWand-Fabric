package dev.smto.constructionwand.api;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.crafting.RecipeWandUpgrade;
import dev.smto.constructionwand.items.core.CoreItem;
import dev.smto.constructionwand.items.wand.WandItem;
import net.minecraft.item.Item;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class ModRegistry {
    public static RegistryKey<Item> createKey(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, ConstructionWand.id(name));
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
