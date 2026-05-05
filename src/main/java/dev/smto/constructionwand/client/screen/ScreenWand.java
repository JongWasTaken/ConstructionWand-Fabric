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
import org.jspecify.annotations.NonNull;

public class ScreenWand extends Screen {
    private final ItemStack wand;
    private final WandOptions wandOptions;

    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING_WIDTH = 50;
    private static final int SPACING_HEIGHT = 30;
    private static final int N_COLS = 2;
    private static final int N_ROWS = 3;

    private static final int FIELD_WIDTH = ScreenWand.N_COLS * (ScreenWand.BUTTON_WIDTH + ScreenWand.SPACING_WIDTH) - ScreenWand.SPACING_WIDTH;
    private static final int FIELD_HEIGHT = ScreenWand.N_ROWS * (ScreenWand.BUTTON_HEIGHT + ScreenWand.SPACING_HEIGHT) - ScreenWand.SPACING_HEIGHT;

    public ScreenWand(ItemStack wand) {
        super(Component.literal("Construction Wand Item Settings"));
        this.wand = wand;
        this.wandOptions = WandOptions.of(wand);
    }

    @Override
    protected void init() {
        this.createButton(0, 0, this.wandOptions.cores);
        this.createButton(0, 1, this.wandOptions.lock);
        this.createButton(0, 2, this.wandOptions.direction);
        this.createButton(1, 0, this.wandOptions.replace);
        this.createButton(1, 1, this.wandOptions.match);
        this.createButton(1, 2, this.wandOptions.random);
        int w = this.font.width(this.wand.getHoverName());
        this.addRenderableOnly(new StringWidget((this.width / 2) - (w / 2), this.getY(0) - 40, w, 20, this.wand.getHoverName(), this.font));
        Component t = Component.translatable("gui.constructionwand.settings_button");
        Button button = Button.builder(t, (bt) -> {
                    this.onClose();
                    Minecraft.getInstance().setScreen(new ScreenSettings(this));
                })
                .pos(2, 2)
                .size(this.font.width(t) + 8, 16)
                .build();
        this.addRenderableWidget(button);
    }

    private void createButton(int cx, int cy, IOption<?> option) {
        Button button = Button.builder(this.getButtonLabel(option), (bt) -> this.clickButton(bt, option))
                .pos(this.getX(cx), this.getY(cy))
                .size(ScreenWand.BUTTON_WIDTH, ScreenWand.BUTTON_HEIGHT)
                .tooltip(this.getButtonTooltip(option))
                .createNarration(x -> this.getButtonLabel(option))
                .build();
        button.active = option.isEnabled();
        this.addRenderableWidget(button);
    }

    private void clickButton(Button button, IOption<?> option) {
        option.next();
        ClientPlayNetworking.send(dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.of(option, true));
        button.setMessage(this.getButtonLabel(option));
        button.setTooltip(this.getButtonTooltip(option));
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent k) {
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
        return this.width / 2 - ScreenWand.FIELD_WIDTH / 2 + n * (ScreenWand.BUTTON_WIDTH + ScreenWand.SPACING_WIDTH);
    }

    private int getY(int n) {
        return this.height / 2 - ScreenWand.FIELD_HEIGHT / 2 + n * (ScreenWand.BUTTON_HEIGHT + ScreenWand.SPACING_HEIGHT) + 20;
    }

    private MutableComponent getButtonLabel(IOption<?> option) {
        return Component.translatable(option.getKeyTranslation()).append(Component.translatable(option.getValueTranslation()));
    }

    private Tooltip getButtonTooltip(IOption<?> option) {
        return Tooltip.create(Component.translatable(option.getDescTranslation()));
    }
}
