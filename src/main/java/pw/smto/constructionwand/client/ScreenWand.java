package pw.smto.constructionwand.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import pw.smto.constructionwand.basics.option.IOption;
import pw.smto.constructionwand.basics.option.WandOptions;

import java.util.ArrayList;
import java.util.Arrays;

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
        super(Text.literal("ScreenWand"));
        this.wand = wand;
        wandOptions = WandOptions.of(wand);
    }

    private ArrayList<ButtonWidget> buttons;

    @Override
    protected void init() {
        buttons = new ArrayList<>();
        createButton(0, 0, wandOptions.cores);
        createButton(0, 1, wandOptions.lock);
        createButton(0, 2, wandOptions.direction);
        createButton(1, 0, wandOptions.replace);
        createButton(1, 1, wandOptions.match);
        createButton(1, 2, wandOptions.random);
    }

    @Override
    public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredTextWithShadow(textRenderer, wand.getName(), width / 2, height / 2 - FIELD_HEIGHT / 2 - SPACING_HEIGHT, 16777215);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    // clicking buttons usually works as-is, but for some reason these were not clickable, so we have to do it manually
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ButtonWidget buttonWidget : buttons) {
            if (buttonWidget.isMouseOver(mouseX, mouseY)) {
                buttonWidget.onPress();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Arrays.stream(MinecraftClient.getInstance().options.allKeys).anyMatch(k -> {
            return k.matchesKey(keyCode,scanCode);
        })) {
            this.close();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    private void createButton(int cx, int cy, IOption<?> option) {
        ButtonWidget button = ButtonWidget.builder(getButtonLabel(option), (bt) -> clickButton(bt, option))
                .position(getX(cx), getY(cy))
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(getButtonTooltip(option))
                .narrationSupplier(x -> (MutableText) getButtonLabel(option))
                .build();
        button.active = option.isEnabled();
        buttons.add(addDrawable(button));
    }

    private void clickButton(ButtonWidget button, IOption<?> option) {
        option.next();
        ClientPlayNetworking.send(pw.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.of(option, true));
        button.setMessage(getButtonLabel(option));
        button.setTooltip(getButtonTooltip(option));
    }

    private int getX(int n) {
        return width / 2 - FIELD_WIDTH / 2 + n * (BUTTON_WIDTH + SPACING_WIDTH);
    }

    private int getY(int n) {
        return height / 2 - FIELD_HEIGHT / 2 + n * (BUTTON_HEIGHT + SPACING_HEIGHT);
    }

    private Text getButtonLabel(IOption<?> option) {
        return Text.translatable(option.getKeyTranslation()).append(Text.translatable(option.getValueTranslation()));
    }

    private Tooltip getButtonTooltip(IOption<?> option) {
        return Tooltip.of(Text.translatable(option.getDescTranslation()));
    }
}
