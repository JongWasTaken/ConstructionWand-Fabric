package dev.smto.constructionwand.wand.undo;

import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DestroySnapshot implements ISnapshot {
    private final BlockState block;
    private final BlockPos pos;

    public DestroySnapshot(BlockState block, BlockPos pos) {
        this.pos = pos;
        this.block = block;
    }

    @Nullable
    public static DestroySnapshot get(Level world, Player player, BlockPos pos) {
        if (!WandUtil.isBlockRemovable(world, player, pos)) return null;

        return new DestroySnapshot(world.getBlockState(pos), pos);
    }

    @Override
    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public BlockState getBlockState() {
        return this.block;
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
    public boolean execute(Level world, Player player, BlockHitResult rayTraceResult) {
        return WandUtil.removeBlock(world, player, this.block, this.pos);
    }

    @Override
    public boolean canRestore(Level world, Player player) {
        // Is position out of world?
        if (!world.isInWorldBounds(this.pos)) return false;

        // Is block modifiable?
        if (!world.mayInteract(player, this.pos)) return false;

        // Ignore blocks and entities when in creative
        if (player.isCreative()) return true;

        // Is block empty or fluid?
        if (!world.isEmptyBlock(this.pos) && !world.getBlockState(this.pos).canBeReplaced(Fluids.EMPTY)) return false;

        return !WandUtil.entitiesCollidingWithBlock(world, this.block, this.pos);
    }

    @Override
    public boolean restore(Level world, Player player) {
        return WandUtil.placeBlock(world, player, this.block, this.pos, null, null);
    }

    @Override
    public void forceRestore(Level world) {
        world.setBlockAndUpdate(this.pos, this.block);
    }
}
