package dev.smto.constructionwand.client.screen;

import dev.smto.constructionwand.ConstructionWandClient;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
        super(Component.literal("Construction Wand Mod Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ConstructionWandClient.CONFIG_MANAGER.read();
        createText("gui.constructionwand.settings", -40);
        createText("gui.constructionwand.settings_text", -15);

        createBooleanOption(1, Component.translatable("gui.constructionwand.require_opt_key_menu"), Component.translatable("gui.constructionwand.require_opt_key_menu.tooltip"), "requireOptKeyForMenu");
        createBooleanOption(2, Component.translatable("gui.constructionwand.require_opt_key_actions"), Component.translatable("gui.constructionwand.require_opt_key_actions.tooltip"), "requireOptKeyForActions");
        int r = BUTTON_WIDTH / 2;
        Button saveButton = Button.builder(Component.translatable("gui.constructionwand.save"), (bt) -> {
                    ConstructionWandClient.CONFIG_MANAGER.write(); // save to disk
                    if (this.parent != null && this.minecraft != null) {
                        minecraft.setScreen(parent);
                    } else this.onClose();
                })
                .pos((int) (width * 0.75) - r, height - 22)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .createNarration(x -> Component.translatable("gui.constructionwand.save"))
                .build();
        this.addRenderableWidget(saveButton);

        Button discardButton = Button.builder(Component.translatable("gui.constructionwand.discard"), (bt) -> {
                    ConstructionWandClient.CONFIG_MANAGER.read(); // reload from disk to discard changes
                    if (this.parent != null && this.minecraft != null) {
                        minecraft.setScreen(parent);
                    } else this.onClose();
                })
                .pos((int) (width * 0.25) - r, height - 22)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .createNarration(x -> Component.translatable("gui.constructionwand.discard"))
                .build();
        this.addRenderableWidget(discardButton);
    }

    private void createText(String key, int yOffset) {
        Component t = Component.translatable(key);
        int w = font.width(t);
        this.addRenderableOnly(new StringWidget((width / 2) - (w / 2), getY(0) + yOffset, w, 20, t, font));
    }

    private void createBooleanOption(int cy, MutableComponent label, MutableComponent buttonTooltip, String fieldKey) {
        StringWidget t = new StringWidget(getX(0), getY(cy), font.width(label), 12, label, font);
        this.addRenderableWidget(t);
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
        ChatFormatting formatting = ChatFormatting.RED;

        if (value) formatting = ChatFormatting.GREEN;
        Button button = Button.builder(Component.literal(String.valueOf(value)).withStyle(formatting), (bt) -> clickBooleanButton(bt, finalField))
                .pos(getX(1) + (BUTTON_WIDTH / 2), getY(cy))
                .size(BUTTON_WIDTH / 2, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(buttonTooltip))
                .createNarration(x -> Component.literal(String.valueOf(finalValue)))
                .build();
        this.addRenderableWidget(button);
    }

    private void clickBooleanButton(Button button, Field target) {
        try {
            boolean newValue = !((boolean)target.get(null));
            target.set(null, newValue);
            ChatFormatting formatting = ChatFormatting.RED;
            if (newValue) formatting = ChatFormatting.GREEN;
            button.setMessage(Component.literal(String.valueOf(newValue)).withStyle(formatting));
        } catch (Throwable ignored) {}
    }

    private int getX(int n) {
        return width / 2 - FIELD_WIDTH / 2 + n * (BUTTON_WIDTH + SPACING_WIDTH);
    }

    private int getY(int n) {
        return height / 2 - FIELD_HEIGHT / 2 + n * (BUTTON_HEIGHT + SPACING_HEIGHT);
    }
}
