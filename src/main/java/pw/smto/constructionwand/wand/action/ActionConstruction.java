package pw.smto.constructionwand.wand.action;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import pw.smto.constructionwand.api.IWandAction;
import pw.smto.constructionwand.api.IWandSupplier;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.wand.undo.ISnapshot;
import pw.smto.constructionwand.wand.undo.PlaceSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Default WandAction. Extends your building on the side you're facing.
 */
public class ActionConstruction implements IWandAction
{
    @Override
    public int getLimit(ItemStack wand) {
        return ConfigServer.getWandProperties(wand.getItem()).getLimit();
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();
        LinkedList<BlockPos> candidates = new LinkedList<>();
        HashSet<BlockPos> allCandidates = new HashSet<>();

        Direction placeDirection = rayTraceResult.getSide();
        BlockState targetBlock = world.getBlockState(rayTraceResult.getBlockPos());
        BlockPos startingPoint = rayTraceResult.getBlockPos().add(placeDirection.getVector());

        // Is place direction allowed by lock?
        if(placeDirection == Direction.UP || placeDirection == Direction.DOWN) {
            if(options.testLock(WandOptions.Lock.NORTHSOUTH) || options.testLock(WandOptions.Lock.EASTWEST))
                candidates.add(startingPoint);
        }
        else if(options.testLock(WandOptions.Lock.HORIZONTAL) || options.testLock(WandOptions.Lock.VERTICAL))
            candidates.add(startingPoint);

        while(!candidates.isEmpty() && placeSnapshots.size() < limit) {
            BlockPos currentCandidate = candidates.removeFirst();
            try {
                BlockPos supportingPoint = currentCandidate.add(placeDirection.getOpposite().getVector());
                BlockState candidateSupportingBlock = world.getBlockState(supportingPoint);

                if(options.matchBlocks(targetBlock.getBlock(), candidateSupportingBlock.getBlock()) &&
                        allCandidates.add(currentCandidate)) {
                    PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentCandidate, rayTraceResult, candidateSupportingBlock);
                    if(snapshot == null) continue;
                    placeSnapshots.add(snapshot);

                    switch(placeDirection) {
                        case DOWN:
                        case UP:
                            if(options.testLock(WandOptions.Lock.NORTHSOUTH)) {
                                candidates.add(currentCandidate.add(Direction.NORTH.getVector()));
                                candidates.add(currentCandidate.add(Direction.SOUTH.getVector()));
                            }
                            if(options.testLock(WandOptions.Lock.EASTWEST)) {
                                candidates.add(currentCandidate.add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.WEST.getVector()));
                            }
                            if(options.testLock(WandOptions.Lock.NORTHSOUTH) && options.testLock(WandOptions.Lock.EASTWEST)) {
                                candidates.add(currentCandidate.add(Direction.NORTH.getVector()).add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.NORTH.getVector()).add(Direction.WEST.getVector()));
                                candidates.add(currentCandidate.add(Direction.SOUTH.getVector()).add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.SOUTH.getVector()).add(Direction.WEST.getVector()));
                            }
                            break;
                        case NORTH:
                        case SOUTH:
                            if(options.testLock(WandOptions.Lock.HORIZONTAL)) {
                                candidates.add(currentCandidate.add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.WEST.getVector()));
                            }
                            if(options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.add(Direction.UP.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()));
                            }
                            if(options.testLock(WandOptions.Lock.HORIZONTAL) && options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.add(Direction.UP.getVector()).add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.UP.getVector()).add(Direction.WEST.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()).add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()).add(Direction.WEST.getVector()));
                            }
                            break;
                        case EAST:
                        case WEST:
                            if(options.testLock(WandOptions.Lock.HORIZONTAL)) {
                                candidates.add(currentCandidate.add(Direction.NORTH.getVector()));
                                candidates.add(currentCandidate.add(Direction.SOUTH.getVector()));
                            }
                            if(options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.add(Direction.UP.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()));
                            }
                            if(options.testLock(WandOptions.Lock.HORIZONTAL) && options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.add(Direction.UP.getVector()).add(Direction.NORTH.getVector()));
                                candidates.add(currentCandidate.add(Direction.UP.getVector()).add(Direction.SOUTH.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()).add(Direction.NORTH.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()).add(Direction.SOUTH.getVector()));
                            }
                            break;
                    }
                }
            } catch(Exception e) {
                // Can't do anything, could be anything.
                // Skip if anything goes wrong.
            }
        }
        return placeSnapshots;
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshotsFromAir(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                               ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        return new ArrayList<>();
    }
}
