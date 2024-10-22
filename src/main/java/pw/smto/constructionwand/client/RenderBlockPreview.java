package pw.smto.constructionwand.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.items.wand.ItemWand;
import pw.smto.constructionwand.wand.WandJob;

import java.util.Set;

public class RenderBlockPreview
{
    private static WandJob wandJob;
    public static Set<BlockPos> undoBlocks;

    public static boolean renderBlockHighlight(WorldRenderContext context, HitResult hitResult) {
        BlockHitResult rtr = hitResult instanceof BlockHitResult ? (BlockHitResult) hitResult : null;
        if(rtr == null) return true;
        PlayerEntity player = context.gameRenderer().getClient().player;
        if(player == null) return true;
        Set<BlockPos> blocks;
        float colorR = 0, colorG = 0, colorB = 0;

        ItemStack wand = WandUtil.holdingWand(player);
        if(wand == null) return true;


        if(!(player.isSneaking() && ClientEvents.isOptKeyDown())) {
            // Use cached wandJob for previews of the same target pos/dir
            // Exception: always update if blockCount < 2 to prevent 1-block previews when block updates
            // from the last placement are lagging
            if(wandJob == null || !compareRTR(wandJob.rayTraceResult, rtr) || !(wandJob.wand.equals(wand))
                    || wandJob.blockCount() < 2) {
                wandJob = ItemWand.getWandJob(player, player.getWorld(), rtr, wand);
            }
            blocks = wandJob.getBlockPositions();
        }
        else {
            blocks = undoBlocks;
            colorG = 1;
        }

        if(blocks == null || blocks.isEmpty()) return true;

        double d0 = player.lastRenderX + (player.getX() - player.lastRenderX) * context.tickCounter().getTickDelta(false);
        double d1 = player.lastRenderY + player.getStandingEyeHeight() + (player.getY() - player.lastRenderY) * context.tickCounter().getTickDelta(false);
        double d2 = player.lastRenderZ + (player.getZ() - player.lastRenderZ) * context.tickCounter().getTickDelta(false);

        for(BlockPos block : blocks) {
            VertexRendering.drawBox(
                context.matrixStack(),
                context.consumers().getBuffer(RenderLayer.getLines()),
                new Box(block).offset(-d0, -d1, -d2),
                colorR, colorG, colorB, 0.4F
            );
        }
        return false;
    }

    public static void reset() {
        wandJob = null;
    }

    private static boolean compareRTR(BlockHitResult rtr1, BlockHitResult rtr2) {
        return rtr1.getBlockPos().equals(rtr2.getBlockPos()) && rtr1.getSide().equals(rtr2.getSide());
    }
}
