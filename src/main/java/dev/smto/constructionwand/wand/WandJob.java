package dev.smto.constructionwand.wand;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandAction;
import dev.smto.constructionwand.api.IWandSupplier;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.items.wand.WandItem;
import dev.smto.constructionwand.wand.supplier.SupplierInventory;
import dev.smto.constructionwand.wand.supplier.SupplierRandom;
import dev.smto.constructionwand.wand.undo.ISnapshot;
import dev.smto.constructionwand.wand.undo.UndoHistory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class WandJob
{
    public final Player player;
    public final Level world;
    public final BlockHitResult rayTraceResult;
    public final WandOptions options;
    public final ItemStack wand;
    public final WandItem wandItem;

    private final IWandAction wandAction;
    private final IWandSupplier wandSupplier;

    private List<ISnapshot> placeSnapshots;

    public WandJob(Player player, Level world, BlockHitResult rayTraceResult, ItemStack wand) {
        this.player = player;
        this.world = world;
        this.rayTraceResult = rayTraceResult;
        this.placeSnapshots = new ArrayList<>();

        // Get wand
        this.wand = wand;
        this.wandItem = (WandItem) wand.getItem();
        options = WandOptions.of(wand);

        // Select wand action and supplier based on options
        wandSupplier = options.random.get() ?
                new SupplierRandom(player, options) : new SupplierInventory(player, options);
        wandAction = options.cores.get().getWandAction();

        wandSupplier.getSupply(getTargetItem(world, rayTraceResult));

        getSnapshots();
    }

    @Nullable
    private static BlockItem getTargetItem(Level world, BlockHitResult rayTraceResult) {
        // Get target item
        Item tgitem = world.getBlockState(rayTraceResult.getBlockPos()).getBlock().asItem();
        if(!(tgitem instanceof BlockItem)) return null;
        return (BlockItem) tgitem;
    }

    private void getSnapshots() {
        int limit;
        // Infinity wand gets enhanced limit in creative mode
        if(player.isCreative() && wandItem == ConstructionWand.getRegistry().getInfinityWand()) limit = ConstructionWand.Config.maxInfinityCreativeRange;
        else limit = Math.min(wandItem.remainingDurability(wand), wandAction.getLimit(wand));

        if(rayTraceResult.getType() == HitResult.Type.BLOCK)
            placeSnapshots = wandAction.getSnapshots(world, player, rayTraceResult, wand, options, wandSupplier, limit);
        else
            placeSnapshots = wandAction.getSnapshotsFromAir(world, player, rayTraceResult, wand, options, wandSupplier, limit);
    }

    public Set<BlockPos> getBlockPositions() {
        return placeSnapshots.stream().map(ISnapshot::getPos).collect(Collectors.toSet());
    }

    public int blockCount() {
        return placeSnapshots.size();
    }

    public boolean run() {
        // Rewrote this whole thing:
        // 1. Try to remove all items from inventory
        //    -> if it fails, return any taken items
        // 2. Execute the snapshot
        //    -> if it fails, return all taken items
        // 3. Finally, damage the wand for the count of the first stack
        // 4. Increase stat
        // Hopefully this prevents any duping issues.

        ArrayList<ISnapshot> executed = new ArrayList<>();

        for(ISnapshot snapshot : placeSnapshots) {
            if(wand.isEmpty() || wandItem.remainingDurability(wand) == 0) break;

            // First try to remove all items from inventory
            List<ItemStack> taken = new ArrayList<>();
            if (!player.isCreative()) {
                boolean success = true;
                for (int i = 0; i < snapshot.getRequiredItems().size(); i++) {
                    if(wandSupplier.takeItemStack(snapshot.getRequiredItems().get(i)) == 0) {
                        taken.add(snapshot.getRequiredItems().get(i));
                    }
                    else {
                        ConstructionWand.LOGGER.info("Item could not be taken!");
                        success = false;
                        break;
                    }
                }

                if (!success) {
                    for (ItemStack item : taken) {
                        // if it fails, return any taken items
                        player.handleExtraItemsCreatedOnUse(item);
                    }
                    continue;
                }
            }

            if(snapshot.execute(world, player, rayTraceResult)) {
                executed.add(snapshot);
                if (!player.isCreative()) {
                    // layered blocks would need multiple right-clicks, so each removes 1 durability
                    wand.hurtAndBreak(snapshot.getRequiredItems().getFirst().getCount(), player, EquipmentSlot.MAINHAND);
                }
            } else {
                snapshot.forceRestore(world);
                for (ItemStack item : taken) {
                    // if it fails, return any taken items
                    player.handleExtraItemsCreatedOnUse(item);
                }
            }
            player.awardStat(ConstructionWand.getRegistry().getUseWandStat(), 1);
        }
        placeSnapshots = executed;

        // Play place sound
        if(!placeSnapshots.isEmpty()) {
            SoundType sound = placeSnapshots.getFirst().getBlockState().getSoundType();
            world.playSound(null, player.blockPosition(), sound.getPlaceSound(), SoundSource.BLOCKS, sound.volume, sound.pitch);

            // Add to job history for undo
            UndoHistory.add(player, world, placeSnapshots);
        }

        return !placeSnapshots.isEmpty();
    }
}