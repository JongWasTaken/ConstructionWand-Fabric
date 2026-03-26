package dev.smto.constructionwand.api;

import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.wand.undo.ISnapshot;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface IWandAction
{
    int getLimit(ItemStack wand);

    List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
                                 ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);

    List<ISnapshot> getSnapshotsFromAir(Level world, Player player, BlockHitResult rayTraceResult,
                                        ItemStack wand, WandOptions options, IWandSupplier supplier, int limit);
}
