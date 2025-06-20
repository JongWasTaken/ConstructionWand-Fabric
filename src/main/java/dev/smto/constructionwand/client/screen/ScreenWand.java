package dev.smto.constructionwand.client.screen;

import dev.smto.constructionwand.ConstructionWandClient;
import dev.smto.constructionwand.basics.option.IOption;
import dev.smto.constructionwand.basics.option.WandOptions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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
        super(Text.literal("Construction Wand Item Settings"));
        this.wand = wand;
        wandOptions = WandOptions.of(wand);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        createButton(0, 0, wandOptions.cores);
        createButton(0, 1, wandOptions.lock);
        createButton(0, 2, wandOptions.direction);
        createButton(1, 0, wandOptions.replace);
        createButton(1, 1, wandOptions.match);
        createButton(1, 2, wandOptions.random);
        int w = textRenderer.getWidth(wand.getName());
        this.addDrawable(new TextWidget((width / 2) - (w / 2), getY(0) - 40, w, 20, wand.getName(), textRenderer));
        Text t = Text.translatable("gui.constructionwand.settings_button");
        ButtonWidget button = ButtonWidget.builder(t, (bt) -> {
                    this.close();
                    MinecraftClient.getInstance().setScreen(new ScreenSettings(this));
                })
                .position(2, 2)
                .size(textRenderer.getWidth(t) + 8, 16)
                .build();
        this.addDrawableChild(button);
    }

    private void createButton(int cx, int cy, IOption<?> option) {
        ButtonWidget button = ButtonWidget.builder(getButtonLabel(option), (bt) -> clickButton(bt, option))
                .position(getX(cx), getY(cy))
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(getButtonTooltip(option))
                .narrationSupplier(x -> getButtonLabel(option))
                .build();
        button.active = option.isEnabled();
        this.addDrawableChild(button);
    }

    private void clickButton(ButtonWidget button, IOption<?> option) {
        option.next();
        ClientPlayNetworking.send(dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.getId(),
                dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.encode(dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.of(option, true)));
        button.setMessage(getButtonLabel(option));
        button.setTooltip(getButtonTooltip(option));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!ConstructionWandClient.optionalMenuKey.isUnbound()) {
            if (ConstructionWandClient.optionalMenuKey.matchesKey(keyCode, scanCode)) {
                this.close();
                ConstructionWandClient.optionalMenuKey.setPressed(false);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private int getX(int n) {
        return width / 2 - FIELD_WIDTH / 2 + n * (BUTTON_WIDTH + SPACING_WIDTH);
    }

    private int getY(int n) {
        return height / 2 - FIELD_HEIGHT / 2 + n * (BUTTON_HEIGHT + SPACING_HEIGHT) + 20;
    }

    private MutableText getButtonLabel(IOption<?> option) {
        return Text.translatable(option.getKeyTranslation()).append(Text.translatable(option.getValueTranslation()));
    }

    private Tooltip getButtonTooltip(IOption<?> option) {
        return Tooltip.of(Text.translatable(option.getDescTranslation()));
    }
}
