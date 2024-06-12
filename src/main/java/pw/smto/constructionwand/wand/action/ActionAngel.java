package pw.smto.constructionwand.wand.action;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import pw.smto.constructionwand.api.IWandAction;
import pw.smto.constructionwand.api.IWandSupplier;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.wand.undo.ISnapshot;
import pw.smto.constructionwand.wand.undo.PlaceSnapshot;

import java.util.LinkedList;
import java.util.List;

public class ActionAngel implements IWandAction
{
    @Override
    public int getLimit(ItemStack wand) {
        return ConfigServer.getWandProperties(wand.getItem()).getAngel();
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();

        Direction placeDirection = rayTraceResult.getSide();
        BlockPos currentPos = rayTraceResult.getBlockPos();
        BlockState supportingBlock = world.getBlockState(currentPos);

        for(int i = 0; i < limit; i++) {
            // TODO: this might be wrong
            currentPos = currentPos.add(placeDirection.getOpposite().getVector());

            PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentPos, rayTraceResult, supportingBlock);
            if(snapshot != null) {
                placeSnapshots.add(snapshot);
                break;
            }
        }
        return placeSnapshots;
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshotsFromAir(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                               ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();
        if(!player.isCreative() && !ConfigServer.ANGEL_FALLING.get() && player.fallDistance > 10) return placeSnapshots;
        Vec3d playerVec = WandUtil.entityPositionVec(player);
        Vec3d lookVec = player.getRotationVector().multiply(2, 2, 2);
        Vec3d placeVec = playerVec.add(lookVec);
        BlockPos currentPos = WandUtil.posFromVec(placeVec);

        PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentPos, rayTraceResult, null);
        if(snapshot != null) placeSnapshots.add(snapshot);
        return placeSnapshots;
    }
}
