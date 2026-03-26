package dev.smto.constructionwand.api;

import dev.smto.constructionwand.basics.option.WandOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SnapshotCreationContext {
    private ItemStack includedItemStack = null;
    private boolean giveBackIncludedItem = true;
    private int primaryItemsToConsumeCount;

    private final Level world;
    private final Player player;
    private final BlockHitResult rayTraceResult;
    private final BlockPos blockPos;
    private final BlockItem item;
    private final BlockState target;
    private final BlockState supportingBlock;
    private final WandOptions wandOptions;

    public boolean shouldGiveBackIncludedItem() {
        return giveBackIncludedItem;
    }

    public void setGiveBackIncludedItem(boolean giveBackIncludedItem) {
        this.giveBackIncludedItem = giveBackIncludedItem;
    }

    public ItemStack getIncludedItemStack() {
        return includedItemStack;
    }

    public void setIncludedItemStack(ItemStack includedItem) {
        this.includedItemStack = includedItem;
    }

    public BlockItem getBlockItem() {
        return item;
    }

    @Nullable
    public WandOptions getWandOptions() {
        return wandOptions;
    }

    public Player getPlayer() {
        return player;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void incrementPrimaryItemsToConsumeCount(int amount) {
        primaryItemsToConsumeCount = primaryItemsToConsumeCount + amount;
    }

    public int getPrimaryItemsToConsumeCount() {
        return primaryItemsToConsumeCount;
    }

    public void setPrimaryItemsToConsumeCount(int primaryItemsToConsumeCount) {
        this.primaryItemsToConsumeCount = primaryItemsToConsumeCount;
    }

    public BlockHitResult getRayTraceResult() {
        return rayTraceResult;
    }

    public BlockState getSupportingBlockState() {
        return supportingBlock;
    }

    public BlockState getTargetBlockState() {
        return target;
    }

    public Level getWorld() {
        return world;
    }

    public SnapshotCreationContext(Level world, BlockState target,
                                   BlockState supportingBlock, BlockHitResult rayTraceResult,
                                   int primaryItemsToConsumeCount, Player player, BlockItem item,
                                   BlockPos blockPos, @Nullable WandOptions wandOptions) {
        this.world = world;
        this.wandOptions = wandOptions;
        this.target = target;
        this.supportingBlock = supportingBlock;
        this.rayTraceResult = rayTraceResult;
        this.primaryItemsToConsumeCount = primaryItemsToConsumeCount;
        this.player = player;
        this.item = item;
        this.blockPos = blockPos;
    }
}
