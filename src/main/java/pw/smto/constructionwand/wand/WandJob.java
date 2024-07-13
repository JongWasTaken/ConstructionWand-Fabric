package pw.smto.constructionwand.wand;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.Registry;
import pw.smto.constructionwand.api.IWandAction;
import pw.smto.constructionwand.api.IWandSupplier;
import pw.smto.constructionwand.basics.ConfigServer;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.items.wand.ItemWand;
import pw.smto.constructionwand.wand.supplier.SupplierInventory;
import pw.smto.constructionwand.wand.supplier.SupplierRandom;
import pw.smto.constructionwand.wand.undo.ISnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WandJob
{
    public final PlayerEntity player;
    public final World world;
    public final BlockHitResult rayTraceResult;
    public final WandOptions options;
    public final ItemStack wand;
    public final ItemWand wandItem;

    private final IWandAction wandAction;
    private final IWandSupplier wandSupplier;

    private List<ISnapshot> placeSnapshots;

    public WandJob(PlayerEntity player, World world, BlockHitResult rayTraceResult, ItemStack wand) {
        this.player = player;
        this.world = world;
        this.rayTraceResult = rayTraceResult;
        this.placeSnapshots = new ArrayList<>();

        // Get wand
        this.wand = wand;
        this.wandItem = (ItemWand) wand.getItem();
        options = new WandOptions(wand);

        // Select wand action and supplier based on options
        wandSupplier = options.random.get() ?
                new SupplierRandom(player, options) : new SupplierInventory(player, options);
        wandAction = options.cores.get().getWandAction();

        wandSupplier.getSupply(getTargetItem(world, rayTraceResult));
    }

    @Nullable
    private static BlockItem getTargetItem(World world, BlockHitResult rayTraceResult) {
        // Get target item
        Item tgitem = world.getBlockState(rayTraceResult.getBlockPos()).getBlock().asItem();
        if(!(tgitem instanceof BlockItem)) return null;
        return (BlockItem) tgitem;
    }

    public void getSnapshots() {
        int limit;
        // Infinity wand gets enhanced limit in creative mode
        if(player.isCreative() && wandItem == Registry.Items.INFINITY_WAND) limit = ConfigServer.LIMIT_CREATIVE.get();
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

    public boolean doIt() {
        ArrayList<ISnapshot> executed = new ArrayList<>();

        for(ISnapshot snapshot : placeSnapshots) {
            if(wand.isEmpty() || wandItem.remainingDurability(wand) == 0) break;

            if(snapshot.execute(world, player, rayTraceResult)) {
                if(player.isCreative()) executed.add(snapshot);
                else {
                    // If the item cant be taken, undo the placement
                    for (ItemStack stack : snapshot.getRequiredItems()) {
                        if(wandSupplier.takeItemStack(stack) == 0) {
                            executed.add(snapshot);
                            // TODO: includedItem should not damage the wand
                            wand.damage(1, player, e -> e.sendToolBreakStatus(Hand.MAIN_HAND));
                        }
                        else {
                            ConstructionWand.LOGGER.info("Item could not be taken. Remove block: " +
                                    snapshot.getBlockState().getBlock().toString());
                            snapshot.forceRestore(world);
                            break;
                        }
                    }
                }
                player.increaseStat(Registry.Stats.USE_WAND, 1);
            }
        }
        placeSnapshots = executed;

        // Play place sound
        if(!placeSnapshots.isEmpty()) {
            BlockSoundGroup sound = placeSnapshots.get(0).getBlockState().getSoundGroup();
            world.playSound(null, player.getBlockPos(), sound.getPlaceSound(), SoundCategory.BLOCKS, sound.volume, sound.pitch);

            // Add to job history for undo
            ConstructionWand.undoHistory.add(player, world, placeSnapshots);
        }

        return !placeSnapshots.isEmpty();
    }
}