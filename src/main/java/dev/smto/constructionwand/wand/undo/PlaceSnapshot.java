package dev.smto.constructionwand.wand.undo;

//import com.simibubi.create.content.decoration.copycat.CopycatBlock;

import dev.smto.constructionwand.api.SnapshotCreationContext;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.integrations.mod.ModCompat;
import dev.smto.constructionwand.wand.WandItemUseContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;

public class PlaceSnapshot implements ISnapshot
{
    private BlockState block;
    private final BlockPos pos;
    private final ItemStack item;
    private final ItemStack includedItem;
    private final boolean giveBackIncludedItem;
    private final BlockState supportingBlock;
    private final boolean targetMode;

    public PlaceSnapshot(BlockState block, BlockPos pos, ItemStack item, BlockState supportingBlock, boolean targetMode, @Nullable ItemStack includedItem, @Nullable boolean giveBackIncludedItem) {
        this.block = block;
        this.pos = pos;
        this.item = item;
        this.supportingBlock = supportingBlock;
        this.targetMode = targetMode;
        this.includedItem = includedItem;
        this.giveBackIncludedItem = giveBackIncludedItem;
    }

    public static PlaceSnapshot get(Level world, Player player, BlockHitResult rayTraceResult,
                                    BlockPos pos, BlockItem item,
                                    BlockState supportingBlock, @Nullable WandOptions options) {
        boolean targetMode = options != null && supportingBlock != null && options.direction.get() == WandOptions.Direction.TARGET;
        BlockState blockState = getPlaceBlockstate(world, player, rayTraceResult, pos, item, supportingBlock, targetMode);
        if(blockState == null) return null;

        int count = 1;
        if (blockState.getProperties().contains(BlockStateProperties.LAYERS)) {
            count = blockState.getValue(BlockStateProperties.LAYERS);
        }
        if (blockState.getProperties().contains(BlockStateProperties.SLAB_TYPE)) {
            if (blockState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE) {
                count = count + 2;
            }
        }

        var context = ModCompat.mutateSnapshot(new SnapshotCreationContext(world, blockState, supportingBlock, rayTraceResult, count, player, item, pos, options));
        return new PlaceSnapshot(blockState, pos, new ItemStack(item, context.getPrimaryItemsToConsumeCount()),
                supportingBlock, targetMode, context.getIncludedItemStack(), context.shouldGiveBackIncludedItem());
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public BlockState getBlockState() {
        return block;
    }

    @Override
    public boolean shouldGiveBackIncludedItem() {
        return giveBackIncludedItem;
    }

    @Override
    public List<ItemStack> getRequiredItems() {
        List<ItemStack> items = new ArrayList<>();
        items.add(item);
        if (includedItem != null) {
            items.add(includedItem);
        }
        return items;
    }

    @Override
    public boolean execute(Level world, Player player, BlockHitResult rayTraceResult) {
        // Recalculate PlaceBlockState, because other blocks might be placed nearby
        // Not doing this may cause game crashes (StackOverflowException) when placing lots of blocks
        // with changing orientation like panes, iron bars or redstone.
        block = getPlaceBlockstate(world, player, rayTraceResult, pos, (BlockItem) item.getItem(), supportingBlock, targetMode);
        if(block == null) return false;
        return WandUtil.placeBlock(world, player, block, pos, item, includedItem);
    }

    @Override
    public boolean canRestore(Level world, Player player) {
        return true;
    }

    @Override
    public boolean restore(Level world, Player player) {
        return WandUtil.removeBlock(world, player, block, pos);
    }

    @Override
    public void forceRestore(Level world) {
        world.removeBlock(pos, true);
    }

    /**
     * Tests if a certain block can be placed by the wand.
     * If it can, returns the blockstate to be placed.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    private static BlockState getPlaceBlockstate(Level world, Player player, BlockHitResult rayTraceResult,
                                                 BlockPos pos, BlockItem item,
                                                 @Nullable BlockState supportingBlock, boolean targetMode) {
        // Is block at pos replaceable?
        var ctx = new WandItemUseContext(world, player, rayTraceResult, pos, item);
        if(!ctx.canPlace()) return null;

        // Can block be placed?
        BlockState blockState = item.getBlock().getStateForPlacement(ctx);
        if(blockState == null || !blockState.canSurvive(world, pos)) return null;

        // Forbidden Tile Entity?
        if(!WandUtil.isTEAllowed(blockState)) return null;

        // No entities colliding?
        if(WandUtil.entitiesCollidingWithBlock(world, blockState, pos)) return null;

        // Copy block properties from supporting block
        if(targetMode && supportingBlock != null) {
            // Block properties to be copied (alignment/rotation properties)
            for(Property property : new Property[]{
                    BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.FACING, BlockStateProperties.FACING_HOPPER,
                    BlockStateProperties.ROTATION_16, BlockStateProperties.AXIS, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE,
                    BlockStateProperties.LAYERS, BlockStateProperties.SLAB_TYPE}) {
                if(supportingBlock.getProperties().contains(property) && blockState.getProperties().contains(property)) {
                    blockState = blockState.setValue(property, supportingBlock.getValue(property));
                }
            }
        }

        return blockState;
    }
}
