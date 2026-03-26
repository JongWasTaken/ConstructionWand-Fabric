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
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.core.api.other.PolymerStat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.RecipeSerializer;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;

public class PolymerRegistry extends ModRegistry {
    private final WandItem stoneWand = new PolymerBreakableWandItem(createKey("stone_wand"), new Item.Properties(), ToolMaterial.STONE);
    private final WandItem ironWand = new PolymerBreakableWandItem(createKey("iron_wand"), new Item.Properties(), ToolMaterial.IRON);
    private final WandItem diamondWand = new PolymerBreakableWandItem(createKey("diamond_wand"), new Item.Properties(), ToolMaterial.DIAMOND);
    private final WandItem infinityWand = new PolymerInfinityWandItem(createKey("infinity_wand"));
    private final CoreItem coreAngel = new PolymerAngelCoreItem(createKey("core_angel"));
    private final CoreItem coreDestruction = new PolymerDestructionCoreItem(createKey("core_destruction"));
    private final Identifier statUseWand = PolymerStat.registerStat(Identifier.fromNamespaceAndPath(ConstructionWand.MOD_ID, "use_wand"), StatFormatter.DEFAULT);
    private final RecipeSerializer<RecipeWandUpgrade> recipeWandUpgrade = RecipeWandUpgrade.Serializer.create();

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
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, stoneWand.registryKey, stoneWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, ironWand.registryKey, ironWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, diamondWand.registryKey, diamondWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, infinityWand.registryKey, infinityWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, coreAngel.registryKey, coreAngel);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, coreDestruction.registryKey, coreDestruction);
        var tabBuilder = PolymerCreativeModeTabUtils.builder();
        tabBuilder.title(Component.literal("Construction Wand"));
        tabBuilder.icon(infinityWand::getDefaultInstance);
        tabBuilder.displayItems((displayContext, entries) -> {
            entries.accept(stoneWand.getDefaultInstance());
            entries.accept(ironWand.getDefaultInstance());
            entries.accept(diamondWand.getDefaultInstance());
            entries.accept(infinityWand.getDefaultInstance());
            entries.accept(coreAngel.getDefaultInstance());
            entries.accept(coreDestruction.getDefaultInstance());
        });
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.fromNamespaceAndPath(MOD_ID, "items"), tabBuilder.build());
        net.minecraft.core.Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ConstructionWand.id("wand_upgrade"), recipeWandUpgrade);
    }
}
