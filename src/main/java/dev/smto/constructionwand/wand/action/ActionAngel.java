package dev.smto.constructionwand.wand.action;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.api.IWandSupplier;
import dev.smto.constructionwand.api.WandConfigEntry;
import dev.smto.constructionwand.basics.WandUtil;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class ActionAngel implements IWandAction {
    @Override
    public int getLimit(ItemStack wand) {
        WandConfigEntry wandConfig;
        try {
            wandConfig = (WandConfigEntry) ConstructionWand.WAND_CONFIG_MAP.get(wand.getItem()).get(null);
        } catch (Throwable ignored) {
            return 1;
        }
        return wandConfig.angelDistance();
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();

        Direction placeDirection = rayTraceResult.getDirection();
        BlockPos currentPos = rayTraceResult.getBlockPos();
        BlockState supportingBlock = world.getBlockState(currentPos);

        for (int i = 0; i < limit; i++) {
            currentPos = currentPos.offset(placeDirection.getOpposite().getUnitVec3i());
            PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentPos, rayTraceResult, supportingBlock);
            if (snapshot != null) {
                placeSnapshots.add(snapshot);
                break;
            }
        }
        return placeSnapshots;
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshotsFromAir(Level world, Player player, BlockHitResult rayTraceResult,
                                               ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();
        if (!player.isCreative() && !ConstructionWand.Config.angelFalling && player.fallDistance > 10)
            return placeSnapshots;
        Vec3 playerVec = WandUtil.entityPositionVec(player);
        Vec3 lookVec = player.getLookAngle().multiply(2, 2, 2);
        Vec3 placeVec = playerVec.add(lookVec);
        BlockPos currentPos = WandUtil.posFromVec(placeVec);

        PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentPos, rayTraceResult, null);
        if (snapshot != null) placeSnapshots.add(snapshot);
        return placeSnapshots;
    }
}
