package pw.smto.constructionwand;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
    private static RegistryKey<Item> createKey(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, ConstructionWand.id(name));
    }
    public static class Items {
        public static List<Item> WANDS = new ArrayList<>();
        public static List<Item> CORES = new ArrayList<>();

        public static final ItemWand STONE_WAND = new ItemWandBasic(createKey("stone_wand"), new Item.Settings(), ToolMaterial.STONE);
        public static final ItemWand IRON_WAND = new ItemWandBasic(createKey("iron_wand"), new Item.Settings(), ToolMaterial.IRON);
        public static final ItemWand DIAMOND_WAND = new ItemWandBasic(createKey("diamond_wand"), new Item.Settings(), ToolMaterial.DIAMOND);
        public static final ItemWand INFINITY_WAND = new ItemWandInfinity(createKey("infinity_wand"));
        public static final ItemCore CORE_ANGEL = new ItemCoreAngel(createKey("core_angel"));
        public static final ItemCore CORE_DESTRUCTION =  new ItemCoreDestruction(createKey("core_destruction"));

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
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.STONE_WAND.registryKey, Items.STONE_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.IRON_WAND.registryKey, Items.IRON_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.DIAMOND_WAND.registryKey, Items.DIAMOND_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.INFINITY_WAND.registryKey, Items.INFINITY_WAND);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.CORE_ANGEL.registryKey, Items.CORE_ANGEL);
        net.minecraft.registry.Registry.register(Registries.ITEM, Items.CORE_DESTRUCTION.registryKey, Items.CORE_DESTRUCTION);

        var tabBuilder = PolymerItemGroupUtils.builder();
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
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID, "items"), tabBuilder.build());
    }
}