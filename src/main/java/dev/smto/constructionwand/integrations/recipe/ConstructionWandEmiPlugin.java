package dev.smto.constructionwand.integrations.recipe;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.ConstructionWandClient;
import dev.smto.constructionwand.api.WandConfigEntry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class ConstructionWandEmiPlugin implements EmiPlugin {
    private static final String baseKey = ConstructionWand.MOD_ID + ".description.";
    private static final String baseKeyItem = "item." + ConstructionWand.MOD_ID + ".";

    @Override
    public void register(EmiRegistry registry) {
        Text optkeyText = Text.translatable(ConstructionWandClient.optKey.getBoundKeyTranslationKey())
                .formatted(Formatting.BLUE);
        Text wandModeText = keyComboText(ConstructionWandClient.Config.requireOptKeyForActions, optkeyText);
        Text wandGuiText = keyComboText(ConstructionWandClient.Config.requireOptKeyForMenu, optkeyText);

        for(Item wand : ConstructionWand.getRegistry().getWands()) {
            WandConfigEntry wandProperties = null;
            try {
                wandProperties = (WandConfigEntry) ConstructionWand.WAND_CONFIG_MAP.get(wand).get(null);
            } catch (Throwable ignored) {}

            String durabilityKey = wand == ConstructionWand.getRegistry().getInfinityWand() ? "unlimited" : "limited";
            Text durabilityText = Text.translatable(baseKey + "durability." + durabilityKey, wandProperties.durability());

            registry.addRecipe(new EmiInfoRecipe(
                    List.of(EmiStack.of(wand)),
                    List.of(Text.translatable(baseKey + "wand",
                            Text.translatable(baseKeyItem + Registries.ITEM.getId(wand).getPath()),
                            wandProperties.range(), durabilityText, optkeyText, wandModeText, wandGuiText)),
                    Identifier.of(ConstructionWand.MOD_ID, Registries.ITEM.getId(wand).getPath() + "_info")
            ));
        }

        for(Item core : ConstructionWand.getRegistry().getCores()) {
            registry.addRecipe(new EmiInfoRecipe(
                    List.of(EmiStack.of(core)),
                    List.of(Text.translatable(baseKey + Registries.ITEM.getId(core).getPath())
                            .append("\n\n")
                            .append(Text.translatable(baseKey + "core", wandModeText))),
                    Identifier.of(ConstructionWand.MOD_ID, Registries.ITEM.getId(core).getPath() + "_info")
            ));
        }
    }

    private Text keyComboText(boolean shiftOpt, Text optkeyText) {
        String key = shiftOpt ? "sneak_opt" : "sneak";
        return Text.translatable(baseKey + "key." + key, optkeyText).formatted(Formatting.BLUE);
    }
}
