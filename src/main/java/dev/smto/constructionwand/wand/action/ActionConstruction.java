package dev.smto.constructionwand.wand.action;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.api.IWandSupplier;
import dev.smto.constructionwand.api.WandConfigEntry;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.wand.undo.ISnapshot;
import dev.smto.constructionwand.wand.undo.PlaceSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Default WandAction. Extends your building on the side you're facing.
 */
public class ActionConstruction implements IWandAction {
    @Override
    public int getLimit(ItemStack wand) {
        WandConfigEntry wandConfig;
        try {
            wandConfig = (WandConfigEntry) ConstructionWand.WAND_CONFIG_MAP.get(wand.getItem()).get(null);
        } catch (Throwable ignored) {
            return 1;
        }
        return wandConfig.range();
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();
        LinkedList<BlockPos> candidates = new LinkedList<>();
        HashSet<BlockPos> allCandidates = new HashSet<>();

        Direction placeDirection = rayTraceResult.getDirection();
        BlockState targetBlock = world.getBlockState(rayTraceResult.getBlockPos());
        BlockPos startingPoint = rayTraceResult.getBlockPos().offset(placeDirection.getUnitVec3i());

        // Is place direction allowed by lock?
        if (placeDirection == Direction.UP || placeDirection == Direction.DOWN) {
            if (options.testLock(WandOptions.Lock.NORTHSOUTH) || options.testLock(WandOptions.Lock.EASTWEST))
                candidates.add(startingPoint);
        } else if (options.testLock(WandOptions.Lock.HORIZONTAL) || options.testLock(WandOptions.Lock.VERTICAL))
            candidates.add(startingPoint);

        while (!candidates.isEmpty() && placeSnapshots.size() < limit) {
            BlockPos currentCandidate = candidates.removeFirst();
            try {
                BlockPos supportingPoint = currentCandidate.offset(placeDirection.getOpposite().getUnitVec3i());
                BlockState candidateSupportingBlock = world.getBlockState(supportingPoint);

                if (options.matchBlocks(targetBlock.getBlock(), candidateSupportingBlock.getBlock()) &&
                        allCandidates.add(currentCandidate)) {
                    PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentCandidate, rayTraceResult, candidateSupportingBlock);
                    if (snapshot == null) continue;
                    placeSnapshots.add(snapshot);

                    switch (placeDirection) {
                        case DOWN:
                        case UP:
                            if (options.testLock(WandOptions.Lock.NORTHSOUTH)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getUnitVec3i()));
                            }
                            if (options.testLock(WandOptions.Lock.EASTWEST)) {
                                candidates.add(currentCandidate.offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.WEST.getUnitVec3i()));
                            }
                            if (options.testLock(WandOptions.Lock.NORTHSOUTH) && options.testLock(WandOptions.Lock.EASTWEST)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getUnitVec3i()).offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.NORTH.getUnitVec3i()).offset(Direction.WEST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getUnitVec3i()).offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getUnitVec3i()).offset(Direction.WEST.getUnitVec3i()));
                            }
                            break;
                        case NORTH:
                        case SOUTH:
                            if (options.testLock(WandOptions.Lock.HORIZONTAL)) {
                                candidates.add(currentCandidate.offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.WEST.getUnitVec3i()));
                            }
                            if (options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()));
                            }
                            if (options.testLock(WandOptions.Lock.HORIZONTAL) && options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()).offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()).offset(Direction.WEST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()).offset(Direction.EAST.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()).offset(Direction.WEST.getUnitVec3i()));
                            }
                            break;
                        case EAST:
                        case WEST:
                            if (options.testLock(WandOptions.Lock.HORIZONTAL)) {
                                candidates.add(currentCandidate.offset(Direction.NORTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.SOUTH.getUnitVec3i()));
                            }
                            if (options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()));
                            }
                            if (options.testLock(WandOptions.Lock.HORIZONTAL) && options.testLock(WandOptions.Lock.VERTICAL)) {
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()).offset(Direction.NORTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.UP.getUnitVec3i()).offset(Direction.SOUTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()).offset(Direction.NORTH.getUnitVec3i()));
                                candidates.add(currentCandidate.offset(Direction.DOWN.getUnitVec3i()).offset(Direction.SOUTH.getUnitVec3i()));
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                // Can't do anything, could be anything.
                // Skip if anything goes wrong.
            }
        }
        return placeSnapshots;
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshotsFromAir(Level world, Player player, BlockHitResult rayTraceResult,
                                               ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        return new ArrayList<>();
    }
}
