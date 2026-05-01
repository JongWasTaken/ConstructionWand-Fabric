package dev.smto.constructionwand.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.smto.constructionwand.ConstructionWandClient;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.client.screen.ScreenWand;
import dev.smto.constructionwand.items.wand.WandItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClientEvents
{
    private static boolean optPressed = false;
    private static long lastClickTime = 0;
    @Environment(EnvType.CLIENT)
    public static void init() {
        // send OPT key state to server
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (client.player == null) return;
            if(WandUtil.getHeldWandOrEmpty(client.player) == null) return;
            boolean optState = isOptKeyDown();
            if(optPressed != optState) {
                optPressed = optState;
                ClientPlayNetworking.send(new dev.smto.constructionwand.Network.Payloads.C2SQueryUndoPayload(optPressed));
            }
        });

        // Sneak+(OPT)+Left click wand to change core
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (client.level == null) return;
            if (lastClickTime + 5 > client.level.getGameTime()) return;
            if(client.options.keyAttack.isDown()) {
                if(client.player == null || !canChangeMode(client.player)) return;
                var target = Minecraft.getInstance().hitResult;
                if (target != null && target.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
                    ItemStack wand = WandUtil.getHeldWandOrEmpty(client.player);
                    if(wand.isEmpty()) return;
                    WandOptions wandOptions = WandOptions.of(wand);
                    wandOptions.cores.next();
                    ClientPlayNetworking.send(dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.of(wandOptions.cores, true));
                    lastClickTime = client.level.getGameTime();
                }
            }
            // menu key, if bound
            if (!ConstructionWandClient.optionalMenuKey.isUnbound()) {
                if (ConstructionWandClient.optionalMenuKey.isDown() && client.player != null) {
                    if (client.screen == null) {
                        ConstructionWandClient.optionalMenuKey.setDown(false);
                        ItemStack wand = WandUtil.getHeldWandOrEmpty(client.player);
                        if(wand.isEmpty()) return;
                        if (client.screen != null) return;
                        client.setScreen(new ScreenWand(wand));
                    }
                }
            }
        });

        // Sneak+(OPT)+Right click wand to open GUI
        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            if(!world.isClientSide()) return InteractionResult.PASS;
            var target = Minecraft.getInstance().hitResult;
            if (canOpenGui(player) && target != null && target.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
                ItemStack wand = WandUtil.getHeldWandOrEmpty(player);
                if(wand.isEmpty()) return InteractionResult.PASS;
                Minecraft.getInstance().setScreen(new ScreenWand(wand));
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(RenderBlockPreview::renderBlockHighlight);
    }

    public static boolean onScroll(double scrollDelta) {
        var client = Minecraft.getInstance();
        if(client.player == null || !canChangeMode(client.player) || scrollDelta == 0) return false;

        ItemStack wand = WandUtil.getHeldWandOrEmpty(client.player);
        if(wand == null) return false;

        WandOptions wandOptions = WandOptions.of(wand);
        wandOptions.lock.next(scrollDelta < 0);
        ClientPlayNetworking.send(dev.smto.constructionwand.Network.Payloads.C2SWandOptionPayload.of(wandOptions.lock, true));

        return true;
    }

    public static boolean isOptKeyDown() {
        // a bit hacky, but allows the user to use any key regardless of conflicts
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), ConstructionWandClient.optKey.key.getValue());
    }

    public static boolean canChangeMode(Player player) {
        return player.isShiftKeyDown() && (isOptKeyDown() || !ConstructionWandClient.Config.requireOptKeyForActions);
    }

    public static boolean canOpenGui(Player player) {
        return player.isShiftKeyDown() && (isOptKeyDown() || !ConstructionWandClient.Config.requireOptKeyForMenu);
    }
}
