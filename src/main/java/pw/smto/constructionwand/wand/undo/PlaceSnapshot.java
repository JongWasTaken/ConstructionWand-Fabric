package pw.smto.constructionwand.wand.undo;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.wand.WandItemUseContext;

public class PlaceSnapshot implements ISnapshot
{
    private BlockState block;
    private final BlockPos pos;
    private final BlockItem item;
    private final BlockState supportingBlock;
    private final boolean targetMode;
    private final int itemCount;

    public PlaceSnapshot(BlockState block, BlockPos pos, BlockItem item, BlockState supportingBlock, boolean targetMode, int itemCount) {
        this.block = block;
        this.pos = pos;
        this.item = item;
        this.supportingBlock = supportingBlock;
        this.targetMode = targetMode;
        this.itemCount = itemCount;
    }

    public static PlaceSnapshot get(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                    BlockPos pos, BlockItem item,
                                    @Nullable BlockState supportingBlock, @Nullable WandOptions options) {
        boolean targetMode = options != null && supportingBlock != null && options.direction.get() == WandOptions.DIRECTION.TARGET;
        BlockState blockState = getPlaceBlockstate(world, player, rayTraceResult, pos, item, supportingBlock, targetMode);
        if(blockState == null) return null;

        int count = 1;
        if (blockState.getProperties().contains(Properties.LAYERS)) {
            count = blockState.get(Properties.LAYERS);
        }

        return new PlaceSnapshot(blockState, pos, item, supportingBlock, targetMode, count);
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
    public ItemStack getRequiredItems() {
        return new ItemStack(item, itemCount);
    }

    @Override
    public boolean execute(World world, PlayerEntity player, BlockHitResult rayTraceResult) {
        // Recalculate PlaceBlockState, because other blocks might be placed nearby
        // Not doing this may cause game crashes (StackOverflowException) when placing lots of blocks
        // with changing orientation like panes, iron bars or redstone.
        block = getPlaceBlockstate(world, player, rayTraceResult, pos, item, supportingBlock, targetMode);
        if(block == null) return false;
        return WandUtil.placeBlock(world, player, block, pos, item);
    }

    @Override
    public boolean canRestore(World world, PlayerEntity player) {
        return true;
    }

    @Override
    public boolean restore(World world, PlayerEntity player) {
        return WandUtil.removeBlock(world, player, block, pos);
    }

    @Override
    public void forceRestore(World world) {
        world.removeBlock(pos, false);
    }

    /**
     * Tests if a certain block can be placed by the wand.
     * If it can, returns the blockstate to be placed.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    private static BlockState getPlaceBlockstate(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                                 BlockPos pos, BlockItem item,
                                                 @Nullable BlockState supportingBlock, boolean targetMode) {
        // Is block at pos replaceable?
        var ctx = new WandItemUseContext(world, player, rayTraceResult, pos, item);
        if(!ctx.canPlace()) return null;

        // Can block be placed?
        BlockState blockState = item.getBlock().getPlacementState(ctx);
        if(blockState == null || !blockState.canPlaceAt(world, pos)) return null;

        // Forbidden Tile Entity?
        if(!WandUtil.isTEAllowed(blockState)) return null;

        // No entities colliding?
        if(WandUtil.entitiesCollidingWithBlock(world, blockState, pos)) return null;

        // Copy block properties from supporting block
        if(targetMode && supportingBlock != null) {
            // Block properties to be copied (alignment/rotation properties)

            for(Property property : new Property[]{
                    Properties.HORIZONTAL_FACING, Properties.FACING, Properties.HOPPER_FACING,
                    Properties.ROTATION, Properties.AXIS, Properties.BLOCK_HALF, Properties.STAIR_SHAPE, Properties.LAYERS}) {
                if(supportingBlock.getProperties().contains(property) && blockState.getProperties().contains(property)) {
                    blockState = blockState.with(property, supportingBlock.get(property));
                }
            }

            // Dont dupe double slabs
            if(supportingBlock.getProperties().contains(Properties.SLAB_TYPE) && blockState.getProperties().contains(Properties.SLAB_TYPE)) {
                SlabType slabType = supportingBlock.get(Properties.SLAB_TYPE);
                if(slabType != SlabType.DOUBLE)
                    blockState = blockState.with(Properties.SLAB_TYPE, slabType);
            }
        }
        return blockState;
    }
}
