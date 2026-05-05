package dev.smto.constructionwand;

import dev.smto.constructionwand.api.ModRegistry;
import dev.smto.constructionwand.crafting.RecipeWandUpgrade;
import dev.smto.constructionwand.items.core.AngelCoreItem;
import dev.smto.constructionwand.items.core.CoreItem;
import dev.smto.constructionwand.items.core.DestructionCoreItem;
import dev.smto.constructionwand.items.wand.BreakableWandItem;
import dev.smto.constructionwand.items.wand.InfinityWandItem;
import dev.smto.constructionwand.items.wand.WandItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.RecipeSerializer;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;

public class DefaultRegistry extends ModRegistry {
    private final WandItem stoneWand = new BreakableWandItem(ModRegistry.createKey("stone_wand"), new Item.Properties(), ToolMaterial.STONE);
    private final WandItem ironWand = new BreakableWandItem(ModRegistry.createKey("iron_wand"), new Item.Properties(), ToolMaterial.IRON);
    private final WandItem diamondWand = new BreakableWandItem(ModRegistry.createKey("diamond_wand"), new Item.Properties(), ToolMaterial.DIAMOND);
    private final WandItem infinityWand = new InfinityWandItem(ModRegistry.createKey("infinity_wand"));
    private final CoreItem coreAngel = new AngelCoreItem(ModRegistry.createKey("core_angel"));
    private final CoreItem coreDestruction = new DestructionCoreItem(ModRegistry.createKey("core_destruction"));
    private final Identifier statUseWand = Identifier.fromNamespaceAndPath(MOD_ID, "use_wand");
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

    public void registerAll() {
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.stoneWand.registryKey, this.stoneWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.ironWand.registryKey, this.ironWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.diamondWand.registryKey, this.diamondWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.infinityWand.registryKey, this.infinityWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.coreAngel.registryKey, this.coreAngel);
        net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, this.coreDestruction.registryKey, this.coreDestruction);
        var tabBuilder = FabricCreativeModeTab.builder();
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
        net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "items"), tabBuilder.build());
        net.minecraft.core.Registry.register(BuiltInRegistries.CUSTOM_STAT, this.statUseWand, this.statUseWand);
        net.minecraft.core.Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ConstructionWand.id("wand_upgrade"), this.recipeWandUpgrade);

    }
}