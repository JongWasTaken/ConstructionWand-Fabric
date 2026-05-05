package dev.smto.constructionwand.integrations.recipe;

import dev.smto.constructionwand.ConstructionWand;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ConstructionWandReiPlugin implements REIClientPlugin {
    private static final String BASE_KEY = ConstructionWand.MOD_ID + ".description.";
    private static final String BASE_KEY_ITEM = "item." + ConstructionWand.MOD_ID + ".";

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // REI is not yet available for 26.1.*
        /*
        Component optkeyText = Component.translatable(ConstructionWandClient.optKey.saveString())
                .withStyle(ChatFormatting.BLUE);
        Component wandModeText = keyComboText(ConstructionWandClient.Config.requireOptKeyForActions, optkeyText);
        Component wandGuiText = keyComboText(ConstructionWandClient.Config.requireOptKeyForMenu, optkeyText);

        for(Item wand : ConstructionWand.getRegistry().getWands()) {
            WandConfigEntry wandProperties = null;
            try {
                wandProperties = (WandConfigEntry) ConstructionWand.WAND_CONFIG_MAP.get(wand).get(null);
            } catch (Throwable ignored) {}

            String durabilityKey = wand == ConstructionWand.getRegistry().getInfinityWand() ? "unlimited" : "limited";
            Component durabilityText = Component.translatable(ConstructionWandReiPlugin.BASE_KEY + "durability." + durabilityKey, wandProperties.durability());

            var es = EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(wand));
            var d = DefaultInformationDisplay.createFromEntry(es, Component.nullToEmpty("This is a test!"));
            d.line(Component.translatable(ConstructionWandReiPlugin.BASE_KEY + "wand",
                    Component.translatable(ConstructionWandReiPlugin.BASE_KEY_ITEM + BuiltInRegistries.ITEM.getKey(wand).getPath()),
                    wandProperties.range(), durabilityText, optkeyText, wandModeText, wandGuiText));
            registry.add(d);
        }

        for(Item core : ConstructionWand.getRegistry().getCores()) {
            var es = EntryStack.of(VanillaEntryTypes.ITEM,new ItemStack(core));
            var d = DefaultInformationDisplay.createFromEntry(es, Component.nullToEmpty("This is a test!"));
            d.line(Component.translatable(ConstructionWandReiPlugin.BASE_KEY + BuiltInRegistries.ITEM.getKey(core).getPath())
                    .append("\n\n")
                    .append(Component.translatable(ConstructionWandReiPlugin.BASE_KEY + "core", wandModeText)));
            registry.add(d);
        }

         */
    }

    private Component keyComboText(boolean shiftOpt, Component optkeyText) {
        String key = shiftOpt ? "sneak_opt" : "sneak";
        return Component.translatable(ConstructionWandReiPlugin.BASE_KEY + "key." + key, optkeyText).withStyle(ChatFormatting.BLUE);
    }
}
