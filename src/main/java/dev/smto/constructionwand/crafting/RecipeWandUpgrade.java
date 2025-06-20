package dev.smto.constructionwand.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.smto.constructionwand.api.WandConfigEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.Registry;
import dev.smto.constructionwand.api.IWandUpgrade;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.items.wand.WandItem;

import java.util.List;

public class RecipeWandUpgrade extends ShapelessRecipe
{
    final String group;
    final CraftingRecipeCategory category = CraftingRecipeCategory.MISC;
    final ItemStack result;
    final DefaultedList<Ingredient> ingredients;
    public RecipeWandUpgrade(String group, CraftingRecipeCategory unused, ItemStack result, DefaultedList<Ingredient> ingredients) {
        super(group, CraftingRecipeCategory.MISC, result, ingredients);
        this.group = group;
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for(int i = 0; i < craftingRecipeInput.getStacks().size(); i++) {
            ItemStack stack = craftingRecipeInput.getStacks().get(i);
            if(!stack.isEmpty()) {
                if(wand == null && stack.getItem() instanceof WandItem) wand = stack;
                else if(upgrade == null && stack.getItem() instanceof IWandUpgrade)
                    upgrade = (IWandUpgrade) stack.getItem();
                else return false;
            }
        }

        if(wand == null || upgrade == null) return false;
        WandConfigEntry wandConfig = null;
        try {
            wandConfig = (WandConfigEntry) ConstructionWand.WAND_CONFIG_MAP.get(wand.getItem()).get(null);
        } catch (Throwable ignored) {}
        return !WandOptions.of(wand).hasUpgrade(upgrade) && wandConfig.upgradeable();
    }

    @NotNull
    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for(int i = 0; i < craftingRecipeInput.getStacks().size(); i++) {
            ItemStack stack = craftingRecipeInput.getStacks().get(i);
            if(!stack.isEmpty()) {
                if(stack.getItem() instanceof WandItem) wand = stack;
                else if(stack.getItem() instanceof IWandUpgrade) upgrade = (IWandUpgrade) stack.getItem();
            }
        }
        //LOGGER.warn("Crafting wand upgrade: " + wand + ", " + upgrade);
        if(wand == null || upgrade == null) return ItemStack.EMPTY;

        ItemStack newWand = wand.copy();

        var u = WandOptions.of(newWand);
        u.addUpgrade(upgrade);
        u.writeToStack();

        return newWand;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registry.RecipeSerializers.WAND_UPGRADE;
    }

    public static class Serializer implements RecipeSerializer<RecipeWandUpgrade> {

        private static RecipeWandUpgrade read(RegistryByteBuf buf) {
            String s = buf.readString();
            int i = buf.readVarInt();
            DefaultedList<Ingredient> nonnulllist = DefaultedList.ofSize(i, Ingredient.EMPTY);

            for(int j = 0; j < nonnulllist.size(); ++j) {
                nonnulllist.set(j, Ingredient.PACKET_CODEC.decode(buf));
            }

            ItemStack itemstack = ItemStack.PACKET_CODEC.decode(buf);
            return new RecipeWandUpgrade(s, null, itemstack, nonnulllist);
        }

        private static void write(RegistryByteBuf buf, RecipeWandUpgrade recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeVarInt(recipe.getIngredients().size());

            for(Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.PACKET_CODEC.encode(buf, ingredient);
            }
            ItemStack.PACKET_CODEC.encode(buf, recipe.getResult(DynamicRegistryManager.EMPTY));
        }

        public static final PacketCodec<RegistryByteBuf, RecipeWandUpgrade> PACKET_CODEC = PacketCodec.ofStatic(
                RecipeWandUpgrade.Serializer::write, RecipeWandUpgrade.Serializer::read
        );

        private static final MapCodec<RecipeWandUpgrade> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                                CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(recipe -> recipe.category),
                                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                                Ingredient.DISALLOW_EMPTY_CODEC
                                        .listOf()
                                        .fieldOf("ingredients")
                                        .flatXmap(
                                                ingredients -> {
                                                    Ingredient[] ingredients2 = ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
                                                    if (ingredients2.length == 0) {
                                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                                    } else {
                                                        return ingredients2.length > 9
                                                                ? DataResult.error(() -> "Too many ingredients for shapeless recipe")
                                                                : DataResult.success(DefaultedList.<Ingredient>copyOf(Ingredient.EMPTY, ingredients2));
                                                    }
                                                },
                                                DataResult::success
                                        )
                                        .forGetter(recipe -> recipe.ingredients)
                        )
                        .apply(instance, RecipeWandUpgrade::new)
        );

        @Override
        public MapCodec<RecipeWandUpgrade> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, RecipeWandUpgrade> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
