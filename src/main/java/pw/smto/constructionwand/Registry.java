package pw.smto.constructionwand;

import io.wispforest.owo.registration.annotations.IterationIgnored;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pw.smto.constructionwand.crafting.RecipeWandUpgrade;
import pw.smto.constructionwand.items.core.ItemCoreAngel;
import pw.smto.constructionwand.items.core.ItemCoreDestruction;
import pw.smto.constructionwand.items.wand.ItemWandBasic;
import pw.smto.constructionwand.items.wand.ItemWandInfinity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static pw.smto.constructionwand.ConstructionWand.MOD_ID;

public class Registry {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface HiddenFromItemGroup {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ItemGroupIcon {}
    private static final List<Item> ITEM_GROUP = new ArrayList<Item>();
    private static ItemStack ITEM_GROUP_ICON = net.minecraft.item.Items.GLASS.getDefaultStack();

    public static class Items implements ItemRegistryContainer {

        @IterationIgnored
        public static List<Item> WANDS = new ArrayList<>();
        @IterationIgnored
        public static List<Item> CORES = new ArrayList<>();

        public static final Item STONE_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.STONE);
        public static final Item IRON_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.IRON);
        public static final Item DIAMOND_WAND = new ItemWandBasic(new Item.Settings(), ToolMaterials.DIAMOND);
        @ItemGroupIcon
        public static final Item INFINITY_WAND = new ItemWandInfinity(new Item.Settings());
        public static final Item CORE_ANGEL = new ItemCoreAngel(new Item.Settings().maxCount(1));
        public static final Item CORE_DESTRUCTION =  new ItemCoreDestruction(new Item.Settings().maxCount(1));

        @Override
        public void postProcessField(String namespace, Item value, String identifier, Field field) {
            if (!field.isAnnotationPresent(HiddenFromItemGroup.class)){
                ITEM_GROUP.add(value);
            }
            if (field.isAnnotationPresent(ItemGroupIcon.class)){
                ITEM_GROUP_ICON = value.getDefaultStack();
            }

            if (identifier.contains("wand")) {
                WANDS.add(value);
            }
            else {
                CORES.add(value);
            }
        }
    }

    public static class Stats implements AutoRegistryContainer<Identifier> {
        public static final Identifier USE_WAND = Identifier.of(ConstructionWand.MOD_ID, "use_wand");

        @Override
        public net.minecraft.registry.Registry<Identifier> getRegistry() {
            return Registries.CUSTOM_STAT;
        }
        @Override
        public Class<Identifier> getTargetFieldType() {
            return Identifier.class;
        }
    }

    public static class RecipeSerializers implements AutoRegistryContainer<RecipeSerializer<?>> {
        public static final RecipeSerializer<RecipeWandUpgrade> WAND_UPGRADE = new RecipeWandUpgrade.Serializer();
        @Override
        public net.minecraft.registry.Registry<RecipeSerializer<?>> getRegistry() {
            return Registries.RECIPE_SERIALIZER;
        }
        @Override
        @SuppressWarnings("unchecked")
        public Class<RecipeSerializer<?>> getTargetFieldType() {
            return (Class<RecipeSerializer<?>>) (Object) RecipeSerializer.class;
        }
    }

    public static void registerAll() {
        FieldRegistrationHandler.register(Items.class, MOD_ID, false);
        FieldRegistrationHandler.register(RecipeSerializers.class, MOD_ID, false);
        FieldRegistrationHandler.register(Stats.class, MOD_ID, false);

        var tabBuilder = new ItemGroup.Builder(null, 0);
        tabBuilder.displayName(Text.literal("Construction Wand"));
        tabBuilder.icon(() -> ITEM_GROUP_ICON);
        tabBuilder.entries((displayContext, entries) -> {
            ITEM_GROUP.forEach(entries::add);
        });
        net.minecraft.registry.Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "items"), tabBuilder.build());
    }
}