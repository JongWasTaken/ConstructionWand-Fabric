package dev.smto.constructionwand.integrations.recipe;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.ConstructionWandClient;
import dev.smto.constructionwand.api.WandConfigEntry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ConstructionWandJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath(ConstructionWand.MOD_ID, ConstructionWand.MOD_ID);
    private static final String BASE_KEY = ConstructionWand.MOD_ID + ".description.";
    private static final String BASE_KEY_ITEM = "item." + ConstructionWand.MOD_ID + ".";

    @Override
    public @NonNull Identifier getPluginUid() {
        return ConstructionWandJeiPlugin.PLUGIN_ID;
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        Component optkeyText = Component.translatable(ConstructionWandClient.optKey.saveString())
                .withStyle(ChatFormatting.BLUE);
        Component wandModeText = this.keyComboText(ConstructionWandClient.Config.requireOptKeyForActions, optkeyText);
        Component wandGuiText = this.keyComboText(ConstructionWandClient.Config.requireOptKeyForMenu, optkeyText);

        for (Item wand : ConstructionWand.getRegistry().getWands()) {
            WandConfigEntry wandProperties;
            try {
                wandProperties = (WandConfigEntry) ConstructionWand.WAND_CONFIG_MAP.get(wand).get(null);
            } catch (Throwable ignored) {
                continue;
            }

            String durabilityKey = wand == ConstructionWand.getRegistry().getInfinityWand() ? "unlimited" : "limited";
            Component durabilityText = Component.translatable(ConstructionWandJeiPlugin.BASE_KEY + "durability." + durabilityKey, wandProperties.durability());

            registration.addIngredientInfo(new ItemStack(wand), VanillaTypes.ITEM_STACK,
                    Component.translatable(ConstructionWandJeiPlugin.BASE_KEY + "wand",
                            Component.translatable(ConstructionWandJeiPlugin.BASE_KEY_ITEM + BuiltInRegistries.ITEM.getKey(wand).getPath()),
                            wandProperties.range(), durabilityText, optkeyText, wandModeText, wandGuiText)
            );
        }

        for (Item core : ConstructionWand.getRegistry().getCores()) {
            registration.addIngredientInfo(new ItemStack(core), VanillaTypes.ITEM_STACK,
                    Component.translatable(ConstructionWandJeiPlugin.BASE_KEY + BuiltInRegistries.ITEM.getKey(core).getPath())
                            .append("\n\n")
                            .append(Component.translatable(ConstructionWandJeiPlugin.BASE_KEY + "core", wandModeText))
            );
        }
    }

    private Component keyComboText(boolean shiftOpt, Component optkeyText) {
        String key = shiftOpt ? "sneak_opt" : "sneak";
        return Component.translatable(ConstructionWandJeiPlugin.BASE_KEY + "key." + key, optkeyText).withStyle(ChatFormatting.BLUE);
    }
}
