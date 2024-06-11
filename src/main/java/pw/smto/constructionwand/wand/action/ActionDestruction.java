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
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.wand.undo.DestroySnapshot;
import pw.smto.constructionwand.wand.undo.ISnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ActionDestruction implements IWandAction
{
    @Override
    public int getLimit(ItemStack wand) {
        return ConfigServer.getWandProperties(wand.getItem()).getDestruction();
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> destroySnapshots = new LinkedList<>();
        // Current list of block positions to process
        LinkedList<BlockPos> candidates = new LinkedList<>();
        // All positions that were processed (dont process blocks multiple times)
        HashSet<BlockPos> allCandidates = new HashSet<>();

        // Block face the wand was pointed at
        Direction breakFace = rayTraceResult.getSide();
        // Block the wand was pointed at
        BlockPos startingPoint = rayTraceResult.getBlockPos();
        BlockState targetBlock = world.getBlockState(rayTraceResult.getBlockPos());

        // Is break direction allowed by lock?
        // Tried to break blocks from top/bottom face, so the wand should allow breaking in NS/EW direction
        if(breakFace == Direction.UP || breakFace == Direction.DOWN) {
            if(options.testLock(WandOptions.LOCK.NORTHSOUTH) || options.testLock(WandOptions.LOCK.EASTWEST))
                candidates.add(startingPoint);
        }
        // Tried to break blocks from side face, so the wand should allow breaking in horizontal/vertical direction
        else if(options.testLock(WandOptions.LOCK.HORIZONTAL) || options.testLock(WandOptions.LOCK.VERTICAL))
            candidates.add(startingPoint);

        // Process current candidates, stop when none are avaiable or block limit is reached
        while(!candidates.isEmpty() && destroySnapshots.size() < limit) {
            BlockPos currentCandidate = candidates.removeFirst();

            // Only break blocks facing the player, with no collidable blocks in between
            if(!WandUtil.isBlockPermeable(world, currentCandidate.add(breakFace.getVector()))) continue;

            try {
                BlockState candidateBlock = world.getBlockState(currentCandidate);

                // If target and candidate blocks match and the current candidate has not been processed
                if(options.matchBlocks(targetBlock.getBlock(), candidateBlock.getBlock()) &&
                        allCandidates.add(currentCandidate)) {
                    DestroySnapshot snapshot = DestroySnapshot.get(world, player, currentCandidate);
                    if(snapshot == null) continue;
                    destroySnapshots.add(snapshot);

                    switch(breakFace) {
                        case DOWN:
                        case UP:
                            if(options.testLock(WandOptions.LOCK.NORTHSOUTH)) {
                                candidates.add(currentCandidate.add(Direction.NORTH.getVector()));
                                candidates.add(currentCandidate.add(Direction.SOUTH.getVector()));
                            }
                            if(options.testLock(WandOptions.LOCK.EASTWEST)) {
                                candidates.add(currentCandidate.add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.WEST.getVector()));
                            }
                            if(options.testLock(WandOptions.LOCK.NORTHSOUTH) && options.testLock(WandOptions.LOCK.EASTWEST)) {
                                candidates.add(currentCandidate.add(Direction.NORTH.getVector()).add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.NORTH.getVector()).add(Direction.WEST.getVector()));
                                candidates.add(currentCandidate.add(Direction.SOUTH.getVector()).add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.SOUTH.getVector()).add(Direction.WEST.getVector()));
                            }
                            break;
                        case NORTH:
                        case SOUTH:
                            if(options.testLock(WandOptions.LOCK.HORIZONTAL)) {
                                candidates.add(currentCandidate.add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.WEST.getVector()));
                            }
                            if(options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.add(Direction.UP.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()));
                            }
                            if(options.testLock(WandOptions.LOCK.HORIZONTAL) && options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.add(Direction.UP.getVector()).add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.UP.getVector()).add(Direction.WEST.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()).add(Direction.EAST.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()).add(Direction.WEST.getVector()));
                            }
                            break;
                        case EAST:
                        case WEST:
                            if(options.testLock(WandOptions.LOCK.HORIZONTAL)) {
                                candidates.add(currentCandidate.add(Direction.NORTH.getVector()));
                                candidates.add(currentCandidate.add(Direction.SOUTH.getVector()));
                            }
                            if(options.testLock(WandOptions.LOCK.VERTICAL)) {
                                candidates.add(currentCandidate.add(Direction.UP.getVector()));
                                candidates.add(currentCandidate.add(Direction.DOWN.getVector()));
                            }
                            if(options.testLock(WandOptions.LOCK.HORIZONTAL) && options.testLock(WandOptions.LOCK.VERTICAL)) {
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
        return destroySnapshots;
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshotsFromAir(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                               ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        return new ArrayList<>();
    }
}
