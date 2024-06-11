package pw.smto.constructionwand.client;

import io.wispforest.owo.ui.event.KeyPress;
import io.wispforest.owo.util.EventSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.Input;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.basics.ConfigClient;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.items.wand.ItemWand;
import pw.smto.constructionwand.network.PacketQueryUndo;
import pw.smto.constructionwand.network.PacketWandOption;

public class ClientEvents
{
    private static boolean optPressed = false;
    private static long lastClickTime = 0;

    public static void init() {
        // send OPT key state to server
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (client.player == null) return;
            if(WandUtil.holdingWand(client.player) == null) return;
            boolean optState = isOptKeyDown();
            if(optPressed != optState) {
                optPressed = optState;
                Network.sendPacket(pw.smto.constructionwand.Network.Channels.C2S_QUERY_UNDO, new pw.smto.constructionwand.Network.PacketData.QueryUndo(optPressed));
            }
        });

        // Sneak+(OPT)+Left click wand to change core
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            //ConstructionWand.LOGGER.warn(lastClickTime);
            if (client.world == null) return;
            if (lastClickTime + 5 > client.world.getTime()) return;
            if(client.options.attackKey.isPressed()) {
                if(client.player == null || !modeKeyCombDown(client.player)) return;
                var target = MinecraftClient.getInstance().crosshairTarget;
                if (target != null && target.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK) {
                    ItemStack wand = client.player.getStackInHand(client.player.getActiveHand());
                    if(!(wand.getItem() instanceof ItemWand)) return;

                    WandOptions wandOptions = new WandOptions(wand);
                    wandOptions.cores.next();
                    Network.sendPacket(pw.smto.constructionwand.Network.Channels.C2S_WAND_OPTION, pw.smto.constructionwand.Network.PacketData.WandOption.of(wandOptions.cores, true));
                    lastClickTime = client.world.getTime();
                }
            }
        });

        // Sneak+(OPT)+Right click wand to open GUI
        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            if(!world.isClient) return TypedActionResult.pass(player.getStackInHand(hand));
            var target = MinecraftClient.getInstance().crosshairTarget;
            if (guiKeyCombDown(player) && target != null && target.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK) {
                ItemStack wand = player.getStackInHand(player.getActiveHand());
                if(!(wand.getItem() instanceof ItemWand)) return TypedActionResult.pass(wand);
                MinecraftClient.getInstance().setScreen(new ScreenWand(wand));
                return TypedActionResult.fail(wand);
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(RenderBlockPreview::renderBlockHighlight);
    }

    public static boolean onScroll(double scrollDelta) {
        var client = MinecraftClient.getInstance();
        if(client.player == null || !modeKeyCombDown(client.player) || scrollDelta == 0) return false;

        ItemStack wand = WandUtil.holdingWand(client.player);
        if(wand == null) return false;

        WandOptions wandOptions = new WandOptions(wand);
        wandOptions.lock.next(scrollDelta < 0);
        Network.sendPacket(pw.smto.constructionwand.Network.Channels.C2S_WAND_OPTION, pw.smto.constructionwand.Network.PacketData.WandOption.of(wandOptions.lock, true));

        return true;
    }

    private static boolean isKeyDown(int id) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), id);
    }

    public static boolean isOptKeyDown() {
        return isKeyDown(ConfigClient.OPT_KEY.get());
    }

    public static boolean modeKeyCombDown(PlayerEntity player) {
        return player.isSneaking() && (isOptKeyDown() || !ConfigClient.SHIFTOPT_MODE.get());
    }

    public static boolean guiKeyCombDown(PlayerEntity player) {
        return player.isSneaking() && (isOptKeyDown() || !ConfigClient.SHIFTOPT_GUI.get());
    }
}
