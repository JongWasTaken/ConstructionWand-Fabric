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
    private ItemStack includedItemStack;
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
        return this.giveBackIncludedItem;
    }

    public void setGiveBackIncludedItem(boolean giveBackIncludedItem) {
        this.giveBackIncludedItem = giveBackIncludedItem;
    }

    public ItemStack getIncludedItemStack() {
        return this.includedItemStack;
    }

    public void setIncludedItemStack(ItemStack includedItem) {
        this.includedItemStack = includedItem;
    }

    public BlockItem getBlockItem() {
        return this.item;
    }

    @Nullable
    public WandOptions getWandOptions() {
        return this.wandOptions;
    }

    public Player getPlayer() {
        return this.player;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public void incrementPrimaryItemsToConsumeCount(int amount) {
        this.primaryItemsToConsumeCount = this.primaryItemsToConsumeCount + amount;
    }

    public int getPrimaryItemsToConsumeCount() {
        return this.primaryItemsToConsumeCount;
    }

    public void setPrimaryItemsToConsumeCount(int primaryItemsToConsumeCount) {
        this.primaryItemsToConsumeCount = primaryItemsToConsumeCount;
    }

    public BlockHitResult getRayTraceResult() {
        return this.rayTraceResult;
    }

    public BlockState getSupportingBlockState() {
        return this.supportingBlock;
    }

    public BlockState getTargetBlockState() {
        return this.target;
    }

    public Level getWorld() {
        return this.world;
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
