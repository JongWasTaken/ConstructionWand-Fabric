package dev.smto.constructionwand;

import dev.smto.constructionwand.api.ModRegistry;
import dev.smto.constructionwand.crafting.RecipeWandUpgrade;
import dev.smto.constructionwand.items.core.AngelCoreItem;
import dev.smto.constructionwand.items.core.CoreItem;
import dev.smto.constructionwand.items.core.DestructionCoreItem;
import dev.smto.constructionwand.items.wand.BreakableWandItem;
import dev.smto.constructionwand.items.wand.InfinityWandItem;
import dev.smto.constructionwand.items.wand.WandItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;
import static dev.smto.constructionwand.ConstructionWand.id;

public class DefaultRegistry extends ModRegistry {
    private final WandItem stoneWand = new BreakableWandItem(new Item.Settings(), ToolMaterials.STONE);
    private final WandItem ironWand = new BreakableWandItem(new Item.Settings(), ToolMaterials.IRON);
    private final WandItem diamondWand = new BreakableWandItem(new Item.Settings(), ToolMaterials.DIAMOND);
    private final WandItem infinityWand = new InfinityWandItem();
    private final CoreItem coreAngel = new AngelCoreItem();
    private final CoreItem coreDestruction = new DestructionCoreItem();
    private final Identifier statUseWand = Identifier.of(ConstructionWand.MOD_ID, "use_wand");
    private final RecipeSerializer<RecipeWandUpgrade> recipeWandUpgrade = new RecipeWandUpgrade.Serializer();

    @Override
    public WandItem getStoneWand() {
        return stoneWand;
    }

    @Override
    public WandItem getIronWand() {
        return ironWand;
    }

    @Override
    public WandItem getDiamondWand() {
        return diamondWand;
    }

    @Override
    public WandItem getInfinityWand() {
        return infinityWand;
    }

    @Override
    public CoreItem getAngelCore() {
        return coreAngel;
    }

    @Override
    public CoreItem getDestructionCore() {
        return coreDestruction;
    }

    @Override
    public Identifier getUseWandStat() {
        return statUseWand;
    }

    @Override
    public RecipeSerializer<RecipeWandUpgrade> getRecipeSerializer() {
        return recipeWandUpgrade;
    }

    public void registerAll() {
        net.minecraft.registry.Registry.register(Registries.ITEM, id("stone_wand"), stoneWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, id("iron_wand"), ironWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, id("diamond_wand"), diamondWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, id("infinity_wand"), infinityWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, id("core_angel"), coreAngel);
        net.minecraft.registry.Registry.register(Registries.ITEM, id("core_destruction"), coreDestruction);
        var tabBuilder = FabricItemGroup.builder();
        tabBuilder.displayName(Text.literal("Construction Wand"));
        tabBuilder.icon(infinityWand::getDefaultStack);
        tabBuilder.entries((displayContext, entries) -> {
            entries.add(stoneWand.getDefaultStack());
            entries.add(ironWand.getDefaultStack());
            entries.add(diamondWand.getDefaultStack());
            entries.add(infinityWand.getDefaultStack());
            entries.add(coreAngel.getDefaultStack());
            entries.add(coreDestruction.getDefaultStack());
        });
        net.minecraft.registry.Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "items"), tabBuilder.build());
        net.minecraft.registry.Registry.register(Registries.CUSTOM_STAT, statUseWand, statUseWand);
        net.minecraft.registry.Registry.register(Registries.RECIPE_SERIALIZER, id("wand_upgrade"), recipeWandUpgrade);

    }
}