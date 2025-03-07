package pw.smto.constructionwand.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.Registry;
import pw.smto.constructionwand.api.IWandUpgrade;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.items.wand.ItemWand;

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
                if(wand == null && stack.getItem() instanceof ItemWand) wand = stack;
                else if(upgrade == null && stack.getItem() instanceof IWandUpgrade)
                    upgrade = (IWandUpgrade) stack.getItem();
                else return false;
            }
        }

        if(wand == null || upgrade == null) return false;
        return !WandOptions.of(wand).hasUpgrade(upgrade) && ConfigServer.getWandProperties(wand.getItem()).isUpgradeable();
    }

    @NotNull
    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for(int i = 0; i < craftingRecipeInput.getStacks().size(); i++) {
            ItemStack stack = craftingRecipeInput.getStacks().get(i);
            if(!stack.isEmpty()) {
                if(stack.getItem() instanceof ItemWand) wand = stack;
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
        return Registry.RecipeSerializers.WAND_UPGRADE;
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
