package pw.smto.constructionwand.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.ConstructionWandClient;

import java.lang.reflect.Field;

public class ScreenSettings extends Screen {
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING_WIDTH = 50;
    private static final int SPACING_HEIGHT = 30;
    private static final int N_COLS = 2;
    private static final int N_ROWS = 3;

    private static final int FIELD_WIDTH = N_COLS * (BUTTON_WIDTH + SPACING_WIDTH) - SPACING_WIDTH;
    private static final int FIELD_HEIGHT = N_ROWS * (BUTTON_HEIGHT + SPACING_HEIGHT) - SPACING_HEIGHT;

    private final Screen parent;

    public ScreenSettings(@Nullable Screen parent) {
        super(Text.literal("Construction Wand Mod Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ConstructionWandClient.CONFIG_MANAGER.read();
        createText("gui.constructionwand.settings", -40);
        createText("gui.constructionwand.settings_text", -15);

        createBooleanOption(1, Text.translatable("gui.constructionwand.require_opt_key_menu"), Text.translatable("gui.constructionwand.require_opt_key_menu.tooltip"), "requireOptKeyForMenu");
        createBooleanOption(2, Text.translatable("gui.constructionwand.require_opt_key_actions"), Text.translatable("gui.constructionwand.require_opt_key_actions.tooltip"), "requireOptKeyForActions");
        int r = BUTTON_WIDTH / 2;
        ButtonWidget saveButton = ButtonWidget.builder(Text.translatable("gui.constructionwand.save"), (bt) -> {
                    ConstructionWandClient.CONFIG_MANAGER.write(); // save to disk
                    if (this.parent != null && this.client != null) {
                        client.setScreen(parent);
                    } else this.close();
                })
                .position((int) (width * 0.75) - r, height - 22)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .narrationSupplier(x -> Text.translatable("gui.constructionwand.save"))
                .build();
        this.addDrawableChild(saveButton);

        ButtonWidget discardButton = ButtonWidget.builder(Text.translatable("gui.constructionwand.discard"), (bt) -> {
                    ConstructionWandClient.CONFIG_MANAGER.read(); // reload from disk to discard changes
                    if (this.parent != null && this.client != null) {
                        client.setScreen(parent);
                    } else this.close();
                })
                .position((int) (width * 0.25) - r, height - 22)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .narrationSupplier(x -> Text.translatable("gui.constructionwand.discard"))
                .build();
        this.addDrawableChild(discardButton);
    }

    private void createText(String key, int yOffset) {
        Text t = Text.translatable(key);
        int w = textRenderer.getWidth(t);
        this.addDrawable(new TextWidget((width / 2) - (w / 2), getY(0) + yOffset, w, 20, t, textRenderer));
    }

    private void createBooleanOption(int cy, MutableText label, MutableText buttonTooltip, String fieldKey) {
        TextWidget t = new TextWidget(getX(0), getY(cy), textRenderer.getWidth(label), 12, label, textRenderer);
        this.addDrawableChild(t);
        Field field;
        Boolean value;
        try {
            field = ConstructionWandClient.Config.class.getField(fieldKey);
            value = (boolean)field.get(null);
        } catch (Throwable ignored) {
            return;
        }
        Field finalField = field;
        Boolean finalValue = value;
        Formatting formatting = Formatting.RED;

        if (value) formatting = Formatting.GREEN;
        ButtonWidget button = ButtonWidget.builder(Text.literal(String.valueOf(value)).formatted(formatting), (bt) -> clickBooleanButton(bt, finalField))
                .position(getX(1) + (BUTTON_WIDTH / 2), getY(cy))
                .size(BUTTON_WIDTH / 2, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(buttonTooltip))
                .narrationSupplier(x -> Text.literal(String.valueOf(finalValue)))
                .build();
        this.addDrawableChild(button);
    }

    private void clickBooleanButton(ButtonWidget button, Field target) {
        try {
            boolean newValue = !((boolean)target.get(null));
            target.set(null, newValue);
            Formatting formatting = Formatting.RED;
            if (newValue) formatting = Formatting.GREEN;
            button.setMessage(Text.literal(String.valueOf(newValue)).formatted(formatting));
        } catch (Throwable ignored) {}
    }

    private int getX(int n) {
        return width / 2 - FIELD_WIDTH / 2 + n * (BUTTON_WIDTH + SPACING_WIDTH);
    }

    private int getY(int n) {
        return height / 2 - FIELD_HEIGHT / 2 + n * (BUTTON_HEIGHT + SPACING_HEIGHT);
    }
}
