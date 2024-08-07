package pw.smto.constructionwand.integrations.recipe;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.Registry;
import pw.smto.constructionwand.basics.ConfigClient;
import pw.smto.constructionwand.basics.ConfigServer;

import java.util.List;

public class ConstructionWandEmiPlugin implements EmiPlugin {
    private static final String baseKey = ConstructionWand.MOD_ID + ".description.";
    private static final String baseKeyItem = "item." + ConstructionWand.MOD_ID + ".";

    @Override
    public void register(EmiRegistry registry) {
        Text optkeyText = Text.translatable(InputUtil.fromKeyCode(ConfigClient.OPT_KEY.get(), -1).getTranslationKey())
                .formatted(Formatting.BLUE);
        Text wandModeText = keyComboText(ConfigClient.SHIFTOPT_MODE.get(), optkeyText);
        Text wandGuiText = keyComboText(ConfigClient.SHIFTOPT_GUI.get(), optkeyText);

        for(Item wand : Registry.Items.WANDS) {
            ConfigServer.WandProperties wandProperties = ConfigServer.getWandProperties(wand);

            String durabilityKey = wand == Registry.Items.INFINITY_WAND ? "unlimited" : "limited";
            Text durabilityText = Text.translatable(baseKey + "durability." + durabilityKey, wandProperties.getDurability());

            registry.addRecipe(new EmiInfoRecipe(
                    List.of(EmiStack.of(wand)),
                    List.of(Text.translatable(baseKey + "wand",
                            Text.translatable(baseKeyItem + Registries.ITEM.getId(wand).getPath()),
                            wandProperties.getLimit(), durabilityText, optkeyText, wandModeText, wandGuiText)),
                    Identifier.of(ConstructionWand.MOD_ID, Registries.ITEM.getId(wand).getPath() + "_info")
            ));
        }

        for(Item core : Registry.Items.CORES) {
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
