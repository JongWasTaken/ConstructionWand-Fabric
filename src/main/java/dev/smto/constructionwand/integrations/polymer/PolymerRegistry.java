package dev.smto.constructionwand.integrations.polymer;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.ModRegistry;
import dev.smto.constructionwand.crafting.RecipeWandUpgrade;
import dev.smto.constructionwand.integrations.polymer.item.PolymerAngelCoreItem;
import dev.smto.constructionwand.integrations.polymer.item.PolymerBreakableWandItem;
import dev.smto.constructionwand.integrations.polymer.item.PolymerDestructionCoreItem;
import dev.smto.constructionwand.integrations.polymer.item.PolymerInfinityWandItem;
import dev.smto.constructionwand.items.core.CoreItem;
import dev.smto.constructionwand.items.wand.WandItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.other.PolymerStat;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.stat.StatFormatter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;

public class PolymerRegistry extends ModRegistry {
    private final WandItem stoneWand = new PolymerBreakableWandItem(createKey("stone_wand"), new Item.Settings(), ToolMaterial.STONE);
    private final WandItem ironWand = new PolymerBreakableWandItem(createKey("iron_wand"), new Item.Settings(), ToolMaterial.IRON);
    private final WandItem diamondWand = new PolymerBreakableWandItem(createKey("diamond_wand"), new Item.Settings(), ToolMaterial.DIAMOND);
    private final WandItem infinityWand = new PolymerInfinityWandItem(createKey("infinity_wand"));
    private final CoreItem coreAngel = new PolymerAngelCoreItem(createKey("core_angel"));
    private final CoreItem coreDestruction = new PolymerDestructionCoreItem(createKey("core_destruction"));
    private final Identifier statUseWand = PolymerStat.registerStat(Identifier.of(ConstructionWand.MOD_ID, "use_wand"), StatFormatter.DEFAULT);
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
    @Override
    public void registerAll() {
        net.minecraft.registry.Registry.register(Registries.ITEM, stoneWand.registryKey, stoneWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, ironWand.registryKey, ironWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, diamondWand.registryKey, diamondWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, infinityWand.registryKey, infinityWand);
        net.minecraft.registry.Registry.register(Registries.ITEM, coreAngel.registryKey, coreAngel);
        net.minecraft.registry.Registry.register(Registries.ITEM, coreDestruction.registryKey, coreDestruction);
        var tabBuilder = PolymerItemGroupUtils.builder();
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
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID, "items"), tabBuilder.build());
        net.minecraft.registry.Registry.register(Registries.RECIPE_SERIALIZER, ConstructionWand.id("wand_upgrade"), recipeWandUpgrade);
    }
}
