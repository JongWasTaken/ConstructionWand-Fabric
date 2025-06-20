package dev.smto.constructionwand.api;

import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.wand.undo.ISnapshot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.List;

public interface IWandAction
{
    int getLimit(ItemStack wand);

    List<ISnapshot> getSnapshots(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                 ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);

    List<ISnapshot> getSnapshotsFromAir(World world, PlayerEntity player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);
}
