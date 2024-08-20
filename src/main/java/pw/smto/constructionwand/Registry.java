package pw.smto.constructionwand;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pw.smto.constructionwand.crafting.RecipeWandUpgrade;
import pw.smto.constructionwand.items.core.ItemCore;
import pw.smto.constructionwand.items.core.ItemCoreAngel;
import pw.smto.constructionwand.items.core.ItemCoreDestruction;
import pw.smto.constructionwand.items.wand.ItemWand;
import pw.smto.constructionwand.items.wand.ItemWandBasic;
import pw.smto.constructionwand.items.wand.ItemWandInfinity;

import java.util.ArrayList;
import java.util.List;

import static pw.smto.constructionwand.ConstructionWand.MOD_ID;

public class Registry {
    public static class Items {
        public static List<Item> WANDS = new ArrayList<>();
        public static List<Item> CORES = new ArrayList<>();

        public static final ItemWand STONE_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.STONE);
        public static final ItemWand IRON_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.IRON);
        public static final ItemWand DIAMOND_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.DIAMOND);
        public static final ItemWand INFINITY_WAND = new ItemWandInfinity();
        public static final ItemCore CORE_ANGEL = new ItemCoreAngel();
        public static final ItemCore CORE_DESTRUCTION =  new ItemCoreDestruction();

        static {
            WANDS.add(STONE_WAND);
            WANDS.add(IRON_WAND);
            WANDS.add(DIAMOND_WAND);
            WANDS.add(INFINITY_WAND);
            CORES.add(CORE_ANGEL);
            CORES.add(CORE_DESTRUCTION);
        }
    }

    public static class Stats {
        public static final Identifier USE_WAND = Identifier.of(ConstructionWand.MOD_ID, "use_wand");
    }

    public static class RecipeSerializers {
        public static final RecipeSerializer<RecipeWandUpgrade> WAND_UPGRADE = new RecipeWandUpgrade.Serializer();
    }

    public static void registerAll() {
        // Items
        net.minecraft.registry.Registry.register(Registries.ITEM, ConstructionWand.id("stone_wand"), Items.STONE_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, ConstructionWand.id("iron_wand"), Items.IRON_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, ConstructionWand.id("diamond_wand"), Items.DIAMOND_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, ConstructionWand.id("infinity_wand"), Items.INFINITY_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, ConstructionWand.id("core_angel"), Items.CORE_ANGEL);
        net.minecraft.registry.Registry.register(Registries.ITEM, ConstructionWand.id("core_destruction"), Items.CORE_DESTRUCTION);

        var tabBuilder = FabricItemGroup.builder();
        tabBuilder.displayName(Text.literal("Construction Wand"));
        tabBuilder.icon(Items.INFINITY_WAND::getDefaultStack);
        tabBuilder.entries((displayContext, entries) -> {
            entries.add(Items.STONE_WAND.getDefaultStack());
            entries.add(Items.IRON_WAND.getDefaultStack());
            entries.add(Items.DIAMOND_WAND.getDefaultStack());
            entries.add(Items.INFINITY_WAND.getDefaultStack());
            entries.add(Items.CORE_ANGEL.getDefaultStack());
            entries.add(Items.CORE_DESTRUCTION.getDefaultStack());
        });
        net.minecraft.registry.Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "items"), tabBuilder.build());

        // Stats
        net.minecraft.registry.Registry.register(Registries.CUSTOM_STAT, Stats.USE_WAND, Stats.USE_WAND);
        
        // Recipe Serializers
        net.minecraft.registry.Registry.register(Registries.RECIPE_SERIALIZER, ConstructionWand.id("wand_upgrade"), RecipeSerializers.WAND_UPGRADE);

    }
}