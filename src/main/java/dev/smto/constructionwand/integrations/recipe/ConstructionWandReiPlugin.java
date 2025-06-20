package dev.smto.constructionwand.integrations.recipe;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.ConstructionWandClient;
import dev.smto.constructionwand.api.WandConfigEntry;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConstructionWandReiPlugin implements REIClientPlugin
{
    private static final String baseKey = ConstructionWand.MOD_ID + ".description.";
    private static final String baseKeyItem = "item." + ConstructionWand.MOD_ID + ".";

    @Override
    public void registerDisplays(DisplayRegistry registry) {
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

            var es = EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(wand));
            var d = DefaultInformationDisplay.createFromEntry(es, Text.of("This is a test!"));
            d.line(Text.translatable(baseKey + "wand",
                    Text.translatable(baseKeyItem + Registries.ITEM.getId(wand).getPath()),
                    wandProperties.range(), durabilityText, optkeyText, wandModeText, wandGuiText));
            registry.add(d);
        }

        for(Item core : ConstructionWand.getRegistry().getCores()) {
            var es = EntryStack.of(VanillaEntryTypes.ITEM,new ItemStack(core));
            var d = DefaultInformationDisplay.createFromEntry(es, Text.of("This is a test!"));
            d.line(Text.translatable(baseKey + Registries.ITEM.getId(core).getPath())
                    .append("\n\n")
                    .append(Text.translatable(baseKey + "core", wandModeText)));
            registry.add(d);
        }
    }

    private Text keyComboText(boolean shiftOpt, Text optkeyText) {
        String key = shiftOpt ? "sneak_opt" : "sneak";
        return Text.translatable(baseKey + "key." + key, optkeyText).formatted(Formatting.BLUE);
    }
}
