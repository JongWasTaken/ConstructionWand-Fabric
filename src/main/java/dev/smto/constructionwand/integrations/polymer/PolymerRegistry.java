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
    private final WandItem stoneWand = new PolymerBreakableWandItem(ModRegistry.createKey("stone_wand"), new Item.Properties(), ToolMaterial.STONE);
    private final WandItem ironWand = new PolymerBreakableWandItem(ModRegistry.createKey("iron_wand"), new Item.Properties(), ToolMaterial.IRON);
    private final WandItem diamondWand = new PolymerBreakableWandItem(ModRegistry.createKey("diamond_wand"), new Item.Properties(), ToolMaterial.DIAMOND);
    private final WandItem infinityWand = new PolymerInfinityWandItem(ModRegistry.createKey("infinity_wand"));
    private final CoreItem coreAngel = new PolymerAngelCoreItem(ModRegistry.createKey("core_angel"));
    private final CoreItem coreDestruction = new PolymerDestructionCoreItem(ModRegistry.createKey("core_destruction"));
    private final Identifier statUseWand = PolymerStat.registerStat(Identifier.fromNamespaceAndPath(MOD_ID, "use_wand"), StatFormatter.DEFAULT);
    private final RecipeSerializer<RecipeWandUpgrade> recipeWandUpgrade = RecipeWandUpgrade.Serializer.create();

    @Override
    public WandItem getStoneWand() {
        return this.stoneWand;
    }

    @Override
    public WandItem getIronWand() {
        return this.ironWand;
    }

    @Override
    public WandItem getDiamondWand() {
        return this.diamondWand;
    }

    @Override
    public WandItem getInfinityWand() {
        return this.infinityWand;
    }

    @Override
    public CoreItem getAngelCore() {
        return this.coreAngel;
    }

    @Override
    public CoreItem getDestructionCore() {
        return this.coreDestruction;
    }

    @Override
    public Identifier getUseWandStat() {
        return this.statUseWand;
    }

    @Override
    public RecipeSerializer<RecipeWandUpgrade> getRecipeSerializer() {
        return this.recipeWandUpgrade;
    }

    @Override
    public void registerAll() {
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.stoneWand.registryKey, this.stoneWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.ironWand.registryKey, this.ironWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.diamondWand.registryKey, this.diamondWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.infinityWand.registryKey, this.infinityWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.coreAngel.registryKey, this.coreAngel);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.coreDestruction.registryKey, this.coreDestruction);
        var tabBuilder = PolymerCreativeModeTabUtils.builder();
        tabBuilder.title(Component.literal("Construction Wand"));
        tabBuilder.icon(this.infinityWand::getDefaultInstance);
        tabBuilder.displayItems((displayContext, entries) -> {
            entries.accept(this.stoneWand.getDefaultInstance());
            entries.accept(this.ironWand.getDefaultInstance());
            entries.accept(this.diamondWand.getDefaultInstance());
            entries.accept(this.infinityWand.getDefaultInstance());
            entries.accept(this.coreAngel.getDefaultInstance());
            entries.accept(this.coreDestruction.getDefaultInstance());
        });
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.fromNamespaceAndPath(MOD_ID, "items"), tabBuilder.build());
        net.minecraft.core.Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ConstructionWand.id("wand_upgrade"), this.recipeWandUpgrade);
    }
}
