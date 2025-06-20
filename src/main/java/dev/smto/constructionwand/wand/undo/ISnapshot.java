package dev.smto.constructionwand.wand.undo;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface ISnapshot
{
    BlockPos getPos();

    BlockState getBlockState();

    List<ItemStack> getRequiredItems();

    boolean shouldGiveBackIncludedItem();

    boolean execute(World world, PlayerEntity player, BlockHitResult rayTraceResult);

    boolean canRestore(World world, PlayerEntity player);

    boolean restore(World world, PlayerEntity player);

    void forceRestore(World world);
}
