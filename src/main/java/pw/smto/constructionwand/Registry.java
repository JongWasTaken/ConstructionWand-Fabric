package pw.smto.constructionwand;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

        public static final ItemWand STONE_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.STONE, ConstructionWand.id("stone_wand"));
        public static final ItemWand IRON_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.IRON, ConstructionWand.id("iron_wand"));
        public static final ItemWand DIAMOND_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.DIAMOND, ConstructionWand.id("diamond_wand"));
        public static final ItemWand INFINITY_WAND = new ItemWandInfinity();
        public static final ItemCore CORE_ANGEL = new ItemCoreAngel(new Item.Settings().maxCount(1));
        public static final ItemCore CORE_DESTRUCTION =  new ItemCoreDestruction(new Item.Settings().maxCount(1));

        static {
            WANDS.add(STONE_WAND);
            WANDS.add(IRON_WAND);
            WANDS.add(DIAMOND_WAND);
            WANDS.add(INFINITY_WAND);

            CORES.add(CORE_ANGEL);
            CORES.add(CORE_DESTRUCTION);
        }
    }

    public static void registerAll() {
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.STONE_WAND.identifier, Items.STONE_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.IRON_WAND.identifier, Items.IRON_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.DIAMOND_WAND.identifier, Items.DIAMOND_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.INFINITY_WAND.identifier, Items.INFINITY_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.CORE_ANGEL.identifier, Items.CORE_ANGEL);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.CORE_DESTRUCTION.identifier, Items.CORE_DESTRUCTION);

        var tabBuilder = new ItemGroup.Builder(null, 0);
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
    }
}