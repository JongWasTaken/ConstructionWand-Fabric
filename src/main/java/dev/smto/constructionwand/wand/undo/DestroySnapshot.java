package dev.smto.constructionwand.wand.undo;

import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DestroySnapshot implements ISnapshot
{
    private final BlockState block;
    private final BlockPos pos;

    public DestroySnapshot(BlockState block, BlockPos pos) {
        this.pos = pos;
        this.block = block;
    }

    @Nullable
    public static DestroySnapshot get(World world, PlayerEntity player, BlockPos pos) {
        if(!WandUtil.isBlockRemovable(world, player, pos)) return null;

        return new DestroySnapshot(world.getBlockState(pos), pos);
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
    public List<ItemStack> getRequiredItems() {
        return List.of(ItemStack.EMPTY);
    }

    @Override
    public boolean shouldGiveBackIncludedItem() {
        return true;
    }

    @Override
    public boolean execute(World world, PlayerEntity player, BlockHitResult rayTraceResult) {
        return WandUtil.removeBlock(world, player, block, pos);
    }

    @Override
    public boolean canRestore(World world, PlayerEntity player) {
        // Is position out of world?
        if(!world.isInBuildLimit(pos)) return false;

        // Is block modifiable?
        if(!world.canEntityModifyAt(player, pos)) return false;

        // Ignore blocks and entities when in creative
        if(player.isCreative()) return true;

        // Is block empty or fluid?
        if(!world.isAir(pos) && !world.getBlockState(pos).canBucketPlace(Fluids.EMPTY)) return false;

        return !WandUtil.entitiesCollidingWithBlock(world, block, pos);
    }

    @Override
    public boolean restore(World world, PlayerEntity player) {
        return WandUtil.placeBlock(world, player, block, pos, null, null);
    }

    @Override
    public void forceRestore(World world) {
        world.setBlockState(pos, block);
    }
}
