package dev.smto.constructionwand.api;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface IModCompatHandler {
    default SnapshotCreationContext onSnapshotCreation(SnapshotCreationContext context) {
        return context;
    }

    default boolean shouldCancelBlockPlacement(World world, PlayerEntity player, BlockState blockState, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) { return false; }

    default void afterBlockPlacement(World world, PlayerEntity player, BlockState blockState, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {}

    default boolean allowBlockEntityRemoval(World world, BlockPos pos, BlockEntity blockEntity) {
        return false;
    }

    default boolean preventWandUseOnBlock(ItemUsageContext context) {
        return false;
    }
}
