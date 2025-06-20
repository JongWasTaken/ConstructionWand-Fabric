package dev.smto.constructionwand.wand;

import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WandItemUseContext extends ItemPlacementContext
{
    public WandItemUseContext(World world, PlayerEntity player, BlockHitResult rayTraceResult, BlockPos pos, BlockItem item) {
        super(world, player, Hand.MAIN_HAND, new ItemStack(item),
                new BlockHitResult(getBlockHitVec(rayTraceResult, pos), rayTraceResult.getSide(), pos, false));
    }

    private static Vec3d getBlockHitVec(BlockHitResult rayTraceResult, BlockPos pos) {
        Vec3d hitVec = rayTraceResult.getPos(); // Absolute coords of hit target

        Vec3d blockDelta = WandUtil.blockPosVec(rayTraceResult.getBlockPos()).subtract(WandUtil.blockPosVec(pos)); // Vector between start and current block

        return blockDelta.add(hitVec); // Absolute coords of current block hit target
    }

    @Override
    public boolean canPlace() {
        return canReplaceExisting;
    }
}
