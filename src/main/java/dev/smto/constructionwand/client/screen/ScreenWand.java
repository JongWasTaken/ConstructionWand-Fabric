package dev.smto.constructionwand.client.screen;

import dev.smto.constructionwand.ConstructionWandClient;
import dev.smto.constructionwand.basics.option.IOption;
import dev.smto.constructionwand.basics.option.WandOptions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ScreenWand extends Screen {
    private final ItemStack wand;
    private final WandOptions wandOptions;

    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING_WIDTH = 50;
    private static final int SPACING_HEIGHT = 30;
    private static final int N_COLS = 2;
    private static final int N_ROWS = 3;

    private static final int FIELD_WIDTH = N_COLS * (BUTTON_WIDTH + SPACING_WIDTH) - SPACING_WIDTH;
    private static final int FIELD_HEIGHT = N_ROWS * (BUTTON_HEIGHT + SPACING_HEIGHT) - SPACING_HEIGHT;

    public ScreenWand(ItemStack wand) {
        super(Component.literal("Construction Wand Item Settings"));
        this.wand = wand;
        wandOptions = WandOptions.of(wand);
    }

    @Override
    protected void init() {
        createButton(0, 0, wandOptions.cores);
        createButton(0, 1, wandOptions.lock);
        createButton(0, 2, wandOptions.direction);
        createButton(1, 0, wandOptions.replace);
        createButton(1, 1, wandOptions.match);
        createButton(1, 2, wandOptions.random);
        int w = font.width(wand.getHoverName());
        this.addRenderableOnly(new StringWidget((width / 2) - (w / 2), getY(0) - 40, w, 20, wand.getHoverName(), font));
        Component t = Component.translatable("gui.constructionwand.settings_button");
        Button button = Button.builder(t, (bt) -> {
                    this.onClose();
                    Minecraft.getInstance().setScreen(new ScreenSettings(this));
                })
                .pos(2, 2)
                .size(font.width(t) + 8, 16)
                .build();
        this.addRenderableWidget(button);
    }

    private void createButton(int cx, int cy, IOption<?> option) {
        Button button = Button.builder(getButtonLabel(option), (bt) -> clickButton(bt, option))
                .pos(getX(cx), getY(cy))
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(getButtonTooltip(option))
                .createNarration(x -> getButtonLabel(option))
                .build();
        button.active = option.isEnabled();
        this.addRenderableWidget(button);
    }

    private void clickButton(Button button, IOption<?> option) {
        option.next();
        ClientPlayNetworking.send(dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.of(option, true));
        button.setMessage(getButtonLabel(option));
        button.setTooltip(getButtonTooltip(option));
    }

    @Override
    public boolean keyPressed(KeyEvent k) {
        if (!ConstructionWandClient.optionalMenuKey.isUnbound()) {
            if (ConstructionWandClient.optionalMenuKey.matches(k)) {
                this.onClose();
                ConstructionWandClient.optionalMenuKey.setDown(false);
                return true;
            }
        }

        return super.keyPressed(k);
    }

    private int getX(int n) {
        return width / 2 - FIELD_WIDTH / 2 + n * (BUTTON_WIDTH + SPACING_WIDTH);
    }

    private int getY(int n) {
        return height / 2 - FIELD_HEIGHT / 2 + n * (BUTTON_HEIGHT + SPACING_HEIGHT) + 20;
    }

    private MutableComponent getButtonLabel(IOption<?> option) {
        return Component.translatable(option.getKeyTranslation()).append(Component.translatable(option.getValueTranslation()));
    }

    private Tooltip getButtonTooltip(IOption<?> option) {
        return Tooltip.create(Component.translatable(option.getDescTranslation()));
    }
}
