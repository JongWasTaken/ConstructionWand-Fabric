package dev.smto.constructionwand;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import dev.smto.constructionwand.crafting.RecipeWandUpgrade;
import dev.smto.constructionwand.items.core.CoreItem;
import dev.smto.constructionwand.items.core.AngelCoreItem;
import dev.smto.constructionwand.items.core.DestructionCoreItem;
import dev.smto.constructionwand.items.wand.WandItem;
import dev.smto.constructionwand.items.wand.BreakableWandItem;
import dev.smto.constructionwand.items.wand.InfinityWandItem;

import java.util.ArrayList;
import java.util.List;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;

public class Registry {
    private static RegistryKey<Item> createKey(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, ConstructionWand.id(name));
    }
    public static class Items {
        public static List<Item> WANDS = new ArrayList<>();
        public static List<Item> CORES = new ArrayList<>();

        public static final WandItem STONE_WAND = new BreakableWandItem(createKey("stone_wand"), new Item.Settings(), ToolMaterial.STONE);
        public static final WandItem IRON_WAND = new BreakableWandItem(createKey("iron_wand"), new Item.Settings(), ToolMaterial.IRON);
        public static final WandItem DIAMOND_WAND = new BreakableWandItem(createKey("diamond_wand"), new Item.Settings(), ToolMaterial.DIAMOND);
        public static final WandItem INFINITY_WAND = new InfinityWandItem(createKey("infinity_wand"));
        public static final CoreItem CORE_ANGEL = new AngelCoreItem(createKey("core_angel"));
        public static final CoreItem CORE_DESTRUCTION =  new DestructionCoreItem(createKey("core_destruction"));

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
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.STONE_WAND.registryKey, Items.STONE_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.IRON_WAND.registryKey, Items.IRON_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.DIAMOND_WAND.registryKey, Items.DIAMOND_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.INFINITY_WAND.registryKey, Items.INFINITY_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.CORE_ANGEL.registryKey, Items.CORE_ANGEL);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.CORE_DESTRUCTION.registryKey, Items.CORE_DESTRUCTION);

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