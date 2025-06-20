package dev.smto.constructionwand.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandUpgrade;
import dev.smto.constructionwand.api.WandConfigEntry;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.items.wand.WandItem;
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

import java.util.List;

public class RecipeWandUpgrade implements CraftingRecipe
{
    public final List<Ingredient> ingredients;
    @Nullable
    private IngredientPlacement ingredientPlacement;

    public RecipeWandUpgrade(List<Ingredient> ingredients) {
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
    public RecipeSerializer<RecipeWandUpgrade> getSerializer() {
        return ConstructionWand.getRegistry().getRecipeSerializer();
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forShapeless(this.ingredients);
        }

        return this.ingredientPlacement;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    public static class Serializer implements RecipeSerializer<RecipeWandUpgrade> {
        private static final MapCodec<RecipeWandUpgrade> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
                        )
                        .apply(instance, RecipeWandUpgrade::new)
        );
        public static final PacketCodec<RegistryByteBuf, RecipeWandUpgrade> PACKET_CODEC = PacketCodec.tuple(
                Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()),
                recipe -> recipe.ingredients,
                RecipeWandUpgrade::new
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
