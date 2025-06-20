package dev.smto.constructionwand.wand.undo;

//import com.simibubi.create.content.decoration.copycat.CopycatBlock;

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
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.integrations.ModCompat;
import dev.smto.constructionwand.wand.WandItemUseContext;

import java.util.ArrayList;
import java.util.List;

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

    public static PlaceSnapshot get(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                    BlockPos pos, BlockItem item,
                                    BlockState supportingBlock, @Nullable WandOptions options) {
        boolean targetMode = options != null && supportingBlock != null && options.direction.get() == WandOptions.Direction.TARGET;
        BlockState blockState = getPlaceBlockstate(world, player, rayTraceResult, pos, item, supportingBlock, targetMode);
        if(blockState == null) return null;

        int count = 1;
        if (blockState.getProperties().contains(Properties.LAYERS)) {
            count = blockState.get(Properties.LAYERS);
        }
        if (blockState.getProperties().contains(Properties.SLAB_TYPE)) {
            if (blockState.get(Properties.SLAB_TYPE) == SlabType.DOUBLE) {
                count = count + 2;
            }
        }

        // Create Copycats compat
        ItemStack includedItem = null;
        boolean giveBackIncludedItem = true;
        // dont bother on the client side, CopycatBlock.getBlockEntity() behaves weirdly there
        if (!world.isClient) {
            if (ModCompat.CREATE) {
                //if (supportingBlock.getBlock() instanceof CopycatBlock b) {
                //    var be = b.getBlockEntity(world, pos.offset(rayTraceResult.getSide().getOpposite()));
                //    includedItem = be.getConsumedItem();
                //    giveBackIncludedItem = false;
                //    if (includedItem.getItem() == Items.AIR) includedItem = null;
                //}
            }
        }
        return new PlaceSnapshot(blockState, pos, new ItemStack(item, count), supportingBlock, targetMode, includedItem, giveBackIncludedItem);
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
    public boolean execute(World world, PlayerEntity player, BlockHitResult rayTraceResult) {
        // Recalculate PlaceBlockState, because other blocks might be placed nearby
        // Not doing this may cause game crashes (StackOverflowException) when placing lots of blocks
        // with changing orientation like panes, iron bars or redstone.
        block = getPlaceBlockstate(world, player, rayTraceResult, pos, (BlockItem) item.getItem(), supportingBlock, targetMode);
        if(block == null) return false;
        return WandUtil.placeBlock(world, player, block, pos, item, includedItem);
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
        world.removeBlock(pos, true);
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
                    Properties.ROTATION, Properties.AXIS, Properties.BLOCK_HALF, Properties.STAIR_SHAPE,
                    Properties.LAYERS, Properties.SLAB_TYPE}) {
                if(supportingBlock.getProperties().contains(property) && blockState.getProperties().contains(property)) {
                    blockState = blockState.with(property, supportingBlock.get(property));
                }
            }
        }

        return blockState;
    }
}
