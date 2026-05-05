package dev.smto.constructionwand.wand.supplier;

import dev.smto.constructionwand.api.IWandSupplier;
import dev.smto.constructionwand.basics.ReplacementRegistry;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.basics.pool.IPool;
import dev.smto.constructionwand.basics.pool.OrderedPool;
import dev.smto.constructionwand.containers.ContainerManager;
import dev.smto.constructionwand.wand.undo.PlaceSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Default WandSupplier. Takes items from player inventory.
 */
public class SupplierInventory implements IWandSupplier {
    protected final Player player;
    protected final WandOptions options;

    protected HashMap<BlockItem, Integer> itemCounts;
    protected IPool<BlockItem> itemPool;

    public SupplierInventory(Player player, WandOptions options) {
        this.player = player;
        this.options = options;
    }

    public void getSupply(@Nullable BlockItem target) {
        this.itemCounts = new LinkedHashMap<>();
        ItemStack offhandStack = this.player.getItemInHand(InteractionHand.OFF_HAND);

        this.itemPool = new OrderedPool<>();

        // Block in offhand -> override
        if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof BlockItem) {
            this.addBlockItem((BlockItem) offhandStack.getItem());
        }
        // Otherwise use target block
        else if (target != null && target != Items.AIR) {
            this.addBlockItem(target);

            // Add replacement items
            if (this.options.match.get() != WandOptions.Match.EXACT) {
                for (Item it : ReplacementRegistry.getMatchingSet(target)) {
                    if (it instanceof BlockItem) this.addBlockItem((BlockItem) it);
                }
            }
        }
    }

    protected void addBlockItem(BlockItem item) {
        int count = WandUtil.countItem(this.player, item);
        if (count > 0) {
            this.itemCounts.put(item, count);
            this.itemPool.add(item);
        }
    }

    @Override
    @Nullable
    public PlaceSnapshot getPlaceSnapshot(Level world, BlockPos pos, BlockHitResult rayTraceResult,
                                          @Nullable BlockState supportingBlock) {
        if (!WandUtil.isPositionPlaceable(world, this.player, pos, this.options.replace.get())) return null;
        this.itemPool.reset();

        while (true) {
            // Draw item from pool (returns null if none are left)
            BlockItem item = this.itemPool.draw();
            if (item == null) return null;

            int count = this.itemCounts.get(item);
            if (count == 0) continue;

            PlaceSnapshot placeSnapshot = PlaceSnapshot.get(world, this.player, rayTraceResult, pos, item, supportingBlock, this.options);
            if (placeSnapshot != null) {
                int newCount = count - 1;
                this.itemCounts.put(item, newCount);

                // Remove item from pool if there are no items left
                if (newCount == 0) this.itemPool.remove(item);

                return placeSnapshot;
            }
        }
    }

    @Override
    public int takeItemStack(ItemStack targetItem) {
        int count = targetItem.getCount();
        Item item = targetItem.getItem();

        if (this.player.getInventory().getNonEquipmentItems().isEmpty()) return count;
        if (this.player.isCreative()) return 0;

        List<ItemStack> hotbar = WandUtil.getHotbarWithOffhand(this.player);
        List<ItemStack> mainInv = WandUtil.getMainInv(this.player);

        // Take items from main inv, loose items first
        count = this.takeItemsInvList(count, item, mainInv, false);
        count = this.takeItemsInvList(count, item, mainInv, true);

        // Take items from hotbar, containers first
        count = this.takeItemsInvList(count, item, hotbar, true);
        count = this.takeItemsInvList(count, item, hotbar, false);

        return count;
    }

    private int takeItemsInvList(int count, Item item, List<ItemStack> inv, boolean container) {
        for (ItemStack stack : inv) {
            if (count == 0) break;

            if (container) {
                count = ContainerManager.useItems(this.player, new ItemStack(item), stack, count);
            }

            if (!container && WandUtil.stackEquals(stack, item)) {
                int toTake = Math.min(count, stack.getCount());
                stack.shrink(toTake);
                count -= toTake;
                this.player.getInventory().setChanged();
            }
        }
        return count;
    }
}
