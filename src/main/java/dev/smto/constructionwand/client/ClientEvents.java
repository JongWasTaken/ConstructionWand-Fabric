package dev.smto.constructionwand.client;

import dev.smto.constructionwand.ConstructionWandClient;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.client.screen.ScreenWand;
import dev.smto.constructionwand.items.wand.WandItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import dev.smto.constructionwand.ConstructionWandClient;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.client.screen.ScreenWand;
import dev.smto.constructionwand.items.wand.WandItem;

public class ClientEvents
{
    private static boolean optPressed = false;
    private static long lastClickTime = 0;
    @Environment(EnvType.CLIENT)
    public static void init() {
        // send OPT key state to server
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (client.player == null) return;
            if(WandUtil.holdingWand(client.player) == null) return;
            boolean optState = isOptKeyDown();
            if(optPressed != optState) {
                optPressed = optState;
                ClientPlayNetworking.send(new dev.smto.constructionwand.Network.Payloads.C2SQueryUndoPayload(optPressed));
            }
        });

        // Sneak+(OPT)+Left click wand to change core
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (client.world == null) return;
            if (lastClickTime + 5 > client.world.getTime()) return;
            if(client.options.attackKey.isPressed()) {
                if(client.player == null || !canChangeMode(client.player)) return;
                var target = MinecraftClient.getInstance().crosshairTarget;
                if (target != null && target.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK) {
                    ItemStack wand = WandUtil.convertPolymerStack(client.player.getStackInHand(client.player.getActiveHand()));
                    if(!(wand.getItem() instanceof WandItem)) return;
                    WandOptions wandOptions = WandOptions.of(wand);
                    wandOptions.cores.next();
                    ClientPlayNetworking.send(dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.of(wandOptions.cores, true));
                    lastClickTime = client.world.getTime();
                }
            }
            // menu key, if bound
            if (!ConstructionWandClient.optionalMenuKey.isUnbound()) {
                if (ConstructionWandClient.optionalMenuKey.isPressed() && client.player != null) {
                    if (client.currentScreen == null) {
                        ConstructionWandClient.optionalMenuKey.setPressed(false);
                        ItemStack wand = WandUtil.convertPolymerStack(client.player.getStackInHand(client.player.getActiveHand()));
                        if(!(wand.getItem() instanceof WandItem)) return;
                        if (client.currentScreen != null) return;
                        client.setScreen(new ScreenWand(wand));
                    }
                }
            }
        });

        // Sneak+(OPT)+Right click wand to open GUI
        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            if(!world.isClient) return TypedActionResult.pass(player.getStackInHand(hand));
            var target = MinecraftClient.getInstance().crosshairTarget;
            if (canOpenGui(player) && target != null && target.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK) {
                ItemStack wand = WandUtil.convertPolymerStack(player.getStackInHand(player.getActiveHand()));
                if(!(wand.getItem() instanceof WandItem)) return ActionResult.PASS;
                MinecraftClient.getInstance().setScreen(new ScreenWand(wand));
                return TypedActionResult.fail(wand);
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(RenderBlockPreview::renderBlockHighlight);
    }

    public static boolean onScroll(double scrollDelta) {
        var client = MinecraftClient.getInstance();
        if(client.player == null || !canChangeMode(client.player) || scrollDelta == 0) return false;

        ItemStack wand = WandUtil.holdingWand(client.player);
        if(wand == null) return false;

        WandOptions wandOptions = WandOptions.of(wand);
        wandOptions.lock.next(scrollDelta < 0);
        ClientPlayNetworking.send(dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.of(wandOptions.lock, true));

        return true;
    }

    public static boolean isOptKeyDown() {
        // a bit hacky, but allows the user to use any key regardless of conflicts
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), ConstructionWandClient.optKey.boundKey.getCode());
    }

    public static boolean canChangeMode(PlayerEntity player) {
        return player.isSneaking() && (isOptKeyDown() || !ConstructionWandClient.Config.requireOptKeyForActions);
    }

    public static boolean canOpenGui(PlayerEntity player) {
        return player.isSneaking() && (isOptKeyDown() || !ConstructionWandClient.Config.requireOptKeyForMenu);
    }
}
