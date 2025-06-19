package pw.smto.constructionwand.integrations.recipe;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.Registry;

public class ConstructionWandReiPlugin implements REIClientPlugin
{
    private static final String baseKey = ConstructionWand.MOD_ID + ".description.";
    private static final String baseKeyItem = "item." + ConstructionWand.MOD_ID + ".";

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        Text optkeyText = Text.translatable(InputUtil.fromKeyCode(ConfigClient.OPT_KEY.get(), -1).getTranslationKey())
                .formatted(Formatting.BLUE);
        Text wandModeText = keyComboText(ConfigClient.SHIFTOPT_MODE.get(), optkeyText);
        Text wandGuiText = keyComboText(ConfigClient.SHIFTOPT_GUI.get(), optkeyText);

        for(Item wand : Registry.Items.WANDS) {
            ConfigServer.WandProperties wandProperties = ConfigServer.getWandProperties(wand);

            String durabilityKey = wand == Registry.Items.INFINITY_WAND ? "unlimited" : "limited";
            Text durabilityText = Text.translatable(baseKey + "durability." + durabilityKey, wandProperties.getDurability());

            var es = EntryStack.of(VanillaEntryTypes.ITEM,new ItemStack(wand));
            var d = DefaultInformationDisplay.createFromEntry(es, Text.of("This is a test!"));
            d.line(Text.translatable(baseKey + "wand",
                    Text.translatable(baseKeyItem + Registries.ITEM.getId(wand).getPath()),
                    wandProperties.getLimit(), durabilityText, optkeyText, wandModeText, wandGuiText));
            registry.add(d);
        }

        for(Item core : Registry.Items.CORES) {
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
