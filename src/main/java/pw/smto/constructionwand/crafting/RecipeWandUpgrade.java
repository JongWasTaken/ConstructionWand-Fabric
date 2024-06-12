package pw.smto.constructionwand.crafting;

import com.google.gson.JsonObject;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
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
    public RecipeWandUpgrade(String group, ItemStack result, DefaultedList<Ingredient> ingredients) {
        super(new Identifier(MOD_ID, "wand_upgrade"), group, CraftingRecipeCategory.MISC, result, ingredients);
    }

    @Override
    public boolean matches(@NotNull RecipeInputInventory inv, @NotNull World worldIn) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for(int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
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
    public ItemStack craft(@NotNull RecipeInputInventory inv, @NotNull DynamicRegistryManager registryAccess) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for(int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if(!stack.isEmpty()) {
                if(stack.getItem() instanceof ItemWand) wand = stack;
                else if(stack.getItem() instanceof IWandUpgrade) upgrade = (IWandUpgrade) stack.getItem();
            }
        }
        LOGGER.warn("Crafting wand upgrade: " + wand + ", " + upgrade);
        if(wand == null || upgrade == null) return ItemStack.EMPTY;

        ItemStack newWand = wand.copy();
        new WandOptions(newWand).addUpgrade(upgrade);
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

        @Override
        public RecipeWandUpgrade read(Identifier id, JsonObject json) {
            /*
            // not supported!
            String type = "constructionwand:wand_upgrade";
            ItemStack result = Items.AIR.getDefaultStack();
            DefaultedList<Ingredient> nonnulllist = DefaultedList.ofSize(2, Ingredient.EMPTY);
            */
            String type = json.get("type").getAsString();
            ItemStack result = Registries.ITEM.get(new Identifier(json.get("result").getAsJsonObject().get("item").getAsString())).getDefaultStack();
            var ilist = json.get("ingredients").getAsJsonArray();
            DefaultedList<Ingredient> nonnulllist = DefaultedList.ofSize(ilist.size(), Ingredient.EMPTY);
            for(int i = 0; i < ilist.size(); ++i) {
                nonnulllist.set(i, Ingredient.fromJson(ilist.get(i).getAsJsonObject()));
            }

            return new RecipeWandUpgrade(type, result, nonnulllist);
        }

        @Override
        public RecipeWandUpgrade read(Identifier identifier, PacketByteBuf pBuffer) {
            String s = pBuffer.readString();
            int i = pBuffer.readVarInt();
            DefaultedList<Ingredient> nonnulllist = DefaultedList.ofSize(i, Ingredient.EMPTY);

            for(int j = 0; j < nonnulllist.size(); ++j) {
                nonnulllist.set(j, Ingredient.fromPacket(pBuffer));
            }

            ItemStack itemstack = pBuffer.readItemStack();
            return new RecipeWandUpgrade(s, itemstack, nonnulllist);
        }


        @Override
        public void write(PacketByteBuf buf, RecipeWandUpgrade recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeVarInt(recipe.getIngredients().size());

            for(Ingredient ingredient : recipe.getIngredients()) {
                ingredient.write(buf);
            }
            buf.writeItemStack(recipe.getOutput(DynamicRegistryManager.EMPTY));
        }

    }
}
