package pw.smto.constructionwand.crafting;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import pw.smto.constructionwand.Registry;
import pw.smto.constructionwand.api.IWandUpgrade;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.items.wand.ItemWand;

import static pw.smto.constructionwand.ConstructionWand.LOGGER;
import static pw.smto.constructionwand.ConstructionWand.MOD_ID;

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
                if(wand == null && stack.getItem() instanceof ItemWand) wand = stack;
                else if(upgrade == null && stack.getItem() instanceof IWandUpgrade)
                    upgrade = (IWandUpgrade) stack.getItem();
                else return false;
            }
        }

        if(wand == null || upgrade == null) return false;
        return !new WandOptions(wand).hasUpgrade(upgrade) && ConfigServer.getWandProperties(wand.getItem()).isUpgradeable();
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
        LOGGER.warn("Crafting wand upgrade: " + wand + ", " + upgrade);
        if(wand == null || upgrade == null) return ItemStack.EMPTY;

        ItemStack newWand = wand.copy();

        var u = new WandOptions(newWand);
        u.addUpgrade(upgrade);
        u.writeToStack(newWand);

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
