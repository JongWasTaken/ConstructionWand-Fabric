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
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;

public class DefaultRegistry extends ModRegistry {
    private final WandItem stoneWand = new BreakableWandItem(createKey("stone_wand"), new Item.Settings(), ToolMaterial.STONE);
    private final WandItem ironWand = new BreakableWandItem(createKey("iron_wand"), new Item.Settings(), ToolMaterial.IRON);
    private final WandItem diamondWand = new BreakableWandItem(createKey("diamond_wand"), new Item.Settings(), ToolMaterial.DIAMOND);
    private final WandItem infinityWand = new InfinityWandItem(createKey("infinity_wand"));
    private final CoreItem coreAngel = new AngelCoreItem(createKey("core_angel"));
    private final CoreItem coreDestruction = new DestructionCoreItem(createKey("core_destruction"));
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
        net.minecraft.registry.Registry.register(Registries.ITEM, stoneWand.registryKey, stoneWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, ironWand.registryKey, ironWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, diamondWand.registryKey, diamondWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, infinityWand.registryKey, infinityWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, coreAngel.registryKey, coreAngel);
        net.minecraft.registry.Registry.register(Registries.ITEM, coreDestruction.registryKey, coreDestruction);
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
        net.minecraft.registry.Registry.register(Registries.RECIPE_SERIALIZER, ConstructionWand.id("wand_upgrade"), recipeWandUpgrade);

    }
}