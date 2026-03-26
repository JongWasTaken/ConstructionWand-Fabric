package dev.smto.constructionwand.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IModCompatHandler {
    default SnapshotCreationContext onSnapshotCreation(SnapshotCreationContext context) {
        return context;
    }

    default boolean shouldCancelBlockPlacement(Level world, Player player, BlockState blockState, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) { return false; }

    default void afterBlockPlacement(Level world, Player player, BlockState blockState, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {}

    default boolean allowBlockEntityRemoval(Level world, BlockPos pos, BlockEntity blockEntity) {
        return false;
    }

    default boolean preventWandUseOnBlock(UseOnContext context) {
        return false;
    }
}
