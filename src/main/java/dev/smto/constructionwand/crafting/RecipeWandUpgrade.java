package dev.smto.constructionwand.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandUpgrade;
import dev.smto.constructionwand.api.WandConfigEntry;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.items.wand.WandItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class RecipeWandUpgrade implements CraftingRecipe {
    public final List<Ingredient> ingredients;
    @Nullable
    private PlacementInfo ingredientPlacement;

    public RecipeWandUpgrade(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean matches(CraftingInput craftingRecipeInput, @NonNull Level world) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for (int i = 0; i < craftingRecipeInput.items().size(); i++) {
            ItemStack stack = craftingRecipeInput.items().get(i);
            if (!stack.isEmpty()) {
                if (wand == null && stack.getItem() instanceof WandItem) wand = stack;
                else if (upgrade == null && stack.getItem() instanceof IWandUpgrade)
                    upgrade = (IWandUpgrade) stack.getItem();
                else return false;
            }
        }

        if (wand == null || upgrade == null) return false;
        WandConfigEntry wandConfig;
        try {
            wandConfig = (WandConfigEntry) ConstructionWand.WAND_CONFIG_MAP.get(wand.getItem()).get(null);
        } catch (Throwable ignored) {
            return false;
        }
        return !WandOptions.of(wand).hasUpgrade(upgrade) && wandConfig.upgradeable();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NonNull String group() {
        return "constructionwand";
    }

    @NotNull
    @Override
    public ItemStack assemble(CraftingInput craftingRecipeInput) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for (int i = 0; i < craftingRecipeInput.items().size(); i++) {
            ItemStack stack = craftingRecipeInput.items().get(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof WandItem) wand = stack;
                else if (stack.getItem() instanceof IWandUpgrade) upgrade = (IWandUpgrade) stack.getItem();
            }
        }
        //LOGGER.warn("Crafting wand upgrade: " + wand + ", " + upgrade);
        if (wand == null || upgrade == null) return ItemStack.EMPTY;

        ItemStack newWand = wand.copy();

        var u = WandOptions.of(newWand);
        u.addUpgrade(upgrade);
        u.writeToStack();

        return newWand;
    }

    @Override
    public @NonNull RecipeSerializer<RecipeWandUpgrade> getSerializer() {
        return ConstructionWand.getRegistry().getRecipeSerializer();
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = PlacementInfo.create(this.ingredients);
        }

        return this.ingredientPlacement;
    }

    @Override
    public @NonNull CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public static class Serializer {
        public static RecipeSerializer<RecipeWandUpgrade> create() {
            return new RecipeSerializer<>(Serializer.CODEC, Serializer.PACKET_CODEC);
        }

        private static final MapCodec<RecipeWandUpgrade> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
                        )
                        .apply(instance, RecipeWandUpgrade::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, RecipeWandUpgrade> PACKET_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
                recipe -> recipe.ingredients,
                RecipeWandUpgrade::new
        );
    }
}
