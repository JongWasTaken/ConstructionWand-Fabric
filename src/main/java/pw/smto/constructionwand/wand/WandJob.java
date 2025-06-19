package pw.smto.constructionwand.wand;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.Registry;
import pw.smto.constructionwand.api.IWandAction;
import pw.smto.constructionwand.api.IWandSupplier;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.items.wand.WandItem;
import pw.smto.constructionwand.wand.supplier.SupplierInventory;
import pw.smto.constructionwand.wand.supplier.SupplierRandom;
import pw.smto.constructionwand.wand.undo.ISnapshot;
import pw.smto.constructionwand.wand.undo.UndoHistory;

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
    public final WandItem wandItem;

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
    private static BlockItem getTargetItem(World world, BlockHitResult rayTraceResult) {
        // Get target item
        Item tgitem = world.getBlockState(rayTraceResult.getBlockPos()).getBlock().asItem();
        if(!(tgitem instanceof BlockItem)) return null;
        return (BlockItem) tgitem;
    }

    private void getSnapshots() {
        int limit;
        // Infinity wand gets enhanced limit in creative mode
        if(player.isCreative() && wandItem == Registry.Items.INFINITY_WAND) limit = ConstructionWand.Config.maxInfinityCreativeRange;
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
                        player.giveOrDropStack(item);
                    }
                    continue;
                }
            }

            if(snapshot.execute(world, player, rayTraceResult)) {
                executed.add(snapshot);
                if (!player.isCreative()) {
                    // layered blocks would need multiple right-clicks, so each removes 1 durability
                    wand.damage(snapshot.getRequiredItems().getFirst().getCount(), player, EquipmentSlot.MAINHAND);
                }
            } else {
                snapshot.forceRestore(world);
                for (ItemStack item : taken) {
                    // if it fails, return any taken items
                    player.giveOrDropStack(item);
                }
            }
            player.increaseStat(Registry.Stats.USE_WAND, 1);
        }
        placeSnapshots = executed;

        // Play place sound
        if(!placeSnapshots.isEmpty()) {
            BlockSoundGroup sound = placeSnapshots.getFirst().getBlockState().getSoundGroup();
            world.playSound(null, player.getBlockPos(), sound.getPlaceSound(), SoundCategory.BLOCKS, sound.volume, sound.pitch);

            // Add to job history for undo
            UndoHistory.add(player, world, placeSnapshots);
        }

        return !placeSnapshots.isEmpty();
    }
}