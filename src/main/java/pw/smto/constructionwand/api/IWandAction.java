package pw.smto.constructionwand.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.wand.undo.ISnapshot;

import java.util.List;

public interface IWandAction
{
    int getLimit(ItemStack wand);

    List<ISnapshot> getSnapshots(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                 ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);

    List<ISnapshot> getSnapshotsFromAir(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);
}
