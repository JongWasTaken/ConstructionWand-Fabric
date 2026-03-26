package dev.smto.constructionwand.client;

import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.wand.WandJob;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import java.util.Set;

public class RenderBlockPreview
{
    private static WandJob wandJob;
    public static Set<BlockPos> undoBlocks;

    public static boolean renderBlockHighlight(LevelRenderContext context, BlockOutlineRenderState outlineRenderState) {
        LocalPlayer player = context.gameRenderer().getMinecraft().player;
        if(player == null) return true;

        var tickCounter = Minecraft.getInstance().getDeltaTracker();
        HitResult hitResult = player.raycastHitResult(tickCounter.getGameTimeDeltaPartialTick(true), context.gameRenderer().getMinecraft().getCameraEntity());

        BlockHitResult rtr = hitResult instanceof BlockHitResult ? (BlockHitResult) hitResult : null;
        if(rtr == null) return true;

        Set<BlockPos> blocks;
        int colorR = 0, colorG = 0, colorB = 0;

        ItemStack wand = WandUtil.holdingWand(player);
        if(wand == null) return true;


        if(!(player.isShiftKeyDown() && ClientEvents.isOptKeyDown())) {
            // Use cached wandJob for previews of the same target pos/dir
            // Exception: always update if blockCount < 2 to prevent 1-block previews when block updates
            // from the last placement are lagging
            if(wandJob == null || !compareRTR(wandJob.rayTraceResult, rtr) || !(wandJob.wand.equals(wand))
                    || wandJob.blockCount() < 2) {
                wandJob = new WandJob(player, player.level(), rtr, wand);
            }
            blocks = wandJob.getBlockPositions();
        }
        else {
            blocks = undoBlocks;
            colorG = 255;
        }

        if(blocks == null || blocks.isEmpty()) return true;

        double d0 = player.xOld + (player.getX() - player.xOld) * tickCounter.getGameTimeDeltaPartialTick(true);
        double d1 = player.yOld + player.getEyeHeight() + (player.getY() - player.yOld) * tickCounter.getGameTimeDeltaPartialTick(true);
        double d2 = player.zOld + (player.getZ() - player.zOld) * tickCounter.getGameTimeDeltaPartialTick(true);

        for(BlockPos block : blocks) {
            ShapeRenderer.renderShape(
                    context.poseStack(),
                    context.bufferSource().getBuffer(RenderTypes.lines()),
                    Shapes.block(),
                    block.getX() -d0, block.getY() -d1, block.getZ() -d2,
                    ARGB.color(colorR, colorG, colorB), 2F
            );
        }

        return false;
    }

    public static void reset() {
        wandJob = null;
    }

    private static boolean compareRTR(BlockHitResult rtr1, BlockHitResult rtr2) {
        return rtr1.getBlockPos().equals(rtr2.getBlockPos()) && rtr1.getDirection().equals(rtr2.getDirection());
    }
}
