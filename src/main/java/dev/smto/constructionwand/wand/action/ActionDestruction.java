package dev.smto.constructionwand.wand.action;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.api.IWandSupplier;
import dev.smto.constructionwand.api.WandConfigEntry;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.wand.undo.DestroySnapshot;
import dev.smto.constructionwand.wand.undo.ISnapshot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ActionDestruction implements IWandAction
{
    @Override
    public int getLimit(ItemStack wand) {
        WandConfigEntry wandConfig = null;
        try {
            wandConfig = (WandConfigEntry) ConstructionWand.WAND_CONFIG_MAP.get(wand.getItem()).get(null);
        } catch (Throwable ignored) {}
        return wandConfig.destructionLimit();
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> destroySnapshots = new LinkedList<>();
        // Current list of block positions to process
        LinkedList<BlockPos> candidates = new LinkedList<>();
        // All positions that were processed (dont process blocks multiple times)
        HashSet<BlockPos> allCandidates = new HashSet<>();

        // Block face the wand was pointed at
        Direction breakFace = rayTraceResult.getDirection();
        // Block the wand was pointed at
        BlockPos startingPoint = rayTraceResult.getBlockPos();
        BlockState targetBlock = world.getBlockState(rayTraceResult.getBlockPos());

        // Is break direction allowed by lock?
        // Tried to break blocks from top/bottom face, so the wand should allow breaking in NS/EW direction
        if(breakFace == Direction.UP || breakFace == Direction.DOWN) {
            if(options.testLock(WandOptions.Lock.NORTHSOUTH) || options.testLock(WandOptions.Lock.EASTWEST))
                candidates.add(startingPoint);
        }
        // Tried to break blocks from side face, so the wand should allow breaking in horizontal/vertical direction
        else if(options.testLock(WandOptions.Lock.HORIZONTAL) || options.testLock(WandOptions.Lock.VERTICAL))
            candidates.add(startingPoint);

        // Process current candidates, stop when none are avaiable or block limit is reached
        while(!candidates.isEmpty() && destroySnapshots.size() < limit) {
            BlockPos currentCandidate = candidates.removeFirst();

            // Only break blocks facing the player, with no collidable blocks in between
            if(!WandUtil.isBlockPermeable(world, currentCandidate.offset(breakFace.getUnitVec3i()))) continue;

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
                            if(options.testLock(WandOptions.Lock.NORTHSOUTH)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getUnitVec3i()));
                            }
                            if(options.testLock(WandOptions.Lock.EASTWEST)) {
                                candidates.add(currentCandidate.offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.WEST.getUnitVec3i()));
                            }
                            if(options.testLock(WandOptions.Lock.NORTHSOUTH) && options.testLock(WandOptions.Lock.EASTWEST)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getUnitVec3i()).offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.NORTH.getUnitVec3i()).offset(Direction.WEST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getUnitVec3i()).offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getUnitVec3i()).offset(Direction.WEST.getUnitVec3i()));
                            }
                            break;
                        case NORTH:
                        case SOUTH:
                            if(options.testLock(WandOptions.Lock.HORIZONTAL)) {
                                candidates.add(currentCandidate.offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.WEST.getUnitVec3i()));
                            }
                            if(options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()));
                            }
                            if(options.testLock(WandOptions.Lock.HORIZONTAL) && options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()).offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()).offset(Direction.WEST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()).offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()).offset(Direction.WEST.getUnitVec3i()));
                            }
                            break;
                        case EAST:
                        case WEST:
                            if(options.testLock(WandOptions.Lock.HORIZONTAL)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getUnitVec3i()));
                            }
                            if(options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()));
                            }
                            if(options.testLock(WandOptions.Lock.HORIZONTAL) && options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()).offset(Direction.NORTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()).offset(Direction.SOUTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()).offset(Direction.NORTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()).offset(Direction.SOUTH.getUnitVec3i()));
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
    public List<ISnapshot> getSnapshotsFromAir(Level world, Player player, BlockHitResult rayTraceResult,
                                               ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        return new ArrayList<>();
    }
}
