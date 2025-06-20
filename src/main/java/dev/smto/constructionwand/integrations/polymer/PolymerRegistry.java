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
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.stat.StatFormatter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;
import static dev.smto.constructionwand.ConstructionWand.id;

public class PolymerRegistry extends ModRegistry {
    private final WandItem stoneWand = new PolymerBreakableWandItem(id("stone_wand"), new Item.Settings(), ToolMaterials.STONE);
    private final WandItem ironWand = new PolymerBreakableWandItem(id("iron_wand"), new Item.Settings(), ToolMaterials.IRON);
    private final WandItem diamondWand = new PolymerBreakableWandItem(id("diamond_wand"), new Item.Settings(), ToolMaterials.DIAMOND);
    private final WandItem infinityWand = new PolymerInfinityWandItem(id("infinity_wand"));
    private final CoreItem coreAngel = new PolymerAngelCoreItem(id("core_angel"));
    private final CoreItem coreDestruction = new PolymerDestructionCoreItem(id("core_destruction"));
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
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID, "items"), tabBuilder.build());
        net.minecraft.registry.Registry.register(Registries.RECIPE_SERIALIZER, id("wand_upgrade"), recipeWandUpgrade);
    }
}
