package pw.smto.constructionwand.wand;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.IOption;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.items.core.ItemCore;

public class WandServerScreen extends SimpleGui {
    private final ItemStack wand;
    private final WandOptions options;
    private final ServerPlayerEntity player;

    private WandServerScreen(ServerPlayerEntity player, ItemStack wand) {
        super(ScreenHandlerType.GENERIC_9X1, player, false);
        this.player = player;
        this.wand = wand;
        this.options = new WandOptions(wand);
        this.setTitle(Text.translatable(wand.getTranslationKey()));
    }

    @Override
    public void beforeOpen() {
        super.beforeOpen();
        for (int i = 0; i < options.allOptions.length-1; i++) {
            this.addSlot(buildElement(options.allOptions[i]));
        }
        this.setSlot(7, GuiElementBuilder.from(Items.MAGMA_BLOCK.getDefaultStack())
                .hideDefaultTooltip()
                .setName(
                        Text.translatable("constructionwand.option.used").formatted(Formatting.AQUA)
                                .append(Text.literal(options.used.getValueString()).formatted(Formatting.GRAY))
                )
                .addLoreLine(Text.translatable("constructionwand.option.used.desc").formatted(Formatting.GRAY))
                .setCallback((int index, ClickType type, SlotActionType action) -> {
                    options.used.set(0);
                    this.close();
                })
                .build()
        );
        this.setSlot(8, GuiElementBuilder.from(Items.DIAMOND.getDefaultStack())
                .hideDefaultTooltip()
                .setName(Text.translatable("constructionwand.option.upgrade").formatted(Formatting.AQUA))
                .addLoreLine(Text.translatable("constructionwand.option.upgrade.desc").formatted(Formatting.GRAY))
                .setCallback((int index, ClickType type, SlotActionType action) -> {
                    WandUtil.upgradeWand(player, wand, options);
                    this.close();
                })
                .build()
        );
    }

    private Text getCapitalizedKey(IOption<?> option) {
        var str = Text.translatable(option.getKey()).getString();
        str = str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
        return Text.literal(str).formatted(Formatting.AQUA);
    }

    private GuiElement buildElement(IOption<?> option) {
        return GuiElementBuilder.from(options.getIcon(option))
                .hideDefaultTooltip()
                .setName(getCapitalizedKey(option))
                .addLoreLine(Text.translatable(option.getValueTranslation()).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(" "))
                .addLoreLine(Text.translatable(option.getDescTranslation()).formatted(Formatting.GRAY))
                .setCallback((int index, ClickType type, SlotActionType action) -> {
                    option.next(!type.isRight);
                    this.setSlot(index, buildElement(option));
                })
                .build();
    }

    @Override
    public void onClose() {
        super.onClose();
        this.options.writeToStack(this.wand);
        player.getInventory().markDirty();
    }

    public static void open(ServerPlayerEntity player, ItemStack wand) {
        new WandServerScreen(player, wand).open();
    }
}
