package pw.smto.constructionwand.client;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandCore;
import pw.smto.constructionwand.basics.option.IOption;
import pw.smto.constructionwand.basics.option.WandOptions;

import java.util.List;

public class TooltipHelper {
    public static void addWandInfo(List<Text> lines, ItemStack itemstack) {
        WandOptions options = new WandOptions(itemstack);
        int limit = options.cores.get().getWandAction().getLimit(itemstack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        // +SHIFT tooltip: show all options + installed cores
        if(net.minecraft.client.gui.screen.Screen.hasShiftDown()) {
            for(int i = 1; i < options.allOptions.length; i++) {
                IOption<?> opt = options.allOptions[i];
                lines.add(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                        .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.GRAY))
                );
            }
            if(!options.cores.getUpgrades().isEmpty()) {
                lines.add(Text.literal(""));
                lines.add(Text.translatable(langTooltip + "cores").formatted(Formatting.GRAY));

                for(IWandCore core : options.cores.getUpgrades()) {
                    lines.add(Text.translatable(options.cores.getKeyTranslation() + "." + core.getRegistryName().toString()));
                }
            }
        }
        // Default tooltip: show block limit + active wand core
        else {
            IOption<?> opt = options.allOptions[0];
            lines.add(Text.translatable(langTooltip + "blocks", limit).formatted(Formatting.GRAY));
            lines.add(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                    .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.WHITE)));
            lines.add(Text.translatable(langTooltip + "shift").formatted(Formatting.AQUA));

        }
    }
}
