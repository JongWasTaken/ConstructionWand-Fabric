package pw.smto.constructionwand.containers.handlers;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.p3pp3rf1y.porting_lib.transfer.items.SCItemStackHandlerSlot;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.common.BackpackWrapperLookup;
import pw.smto.constructionwand.api.IContainerHandler;
import pw.smto.constructionwand.basics.WandUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class HandlerSophisticatedBackpack implements IContainerHandler {
    @Override
    public boolean matches(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        return inventoryStack != null && inventoryStack.getCount() == 1 && inventoryStack.getItem() instanceof BackpackItem;
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        AtomicInteger count = new AtomicInteger(0);
        BackpackWrapperLookup.get(inventoryStack).ifPresent(backpack -> {
            for (SCItemStackHandlerSlot storageViews : backpack.getInventoryHandler().getSlotsContaining(itemStack.getItem())) {
                count.addAndGet((int)storageViews.getAmount());
            }
        });
        /*
                        BackpackWrapperLookup.get(inventoryStack).ifPresent(backpack -> {
            backpack.getInventoryHandler().getSlots().forEach(slot -> {
                if (WandUtil.stackEquals(slot.getResource().toStack(), itemStack)) {
                    count.addAndGet((int) slot.getAmount());
                }
            });
        });
         */
        return count.get();
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        AtomicInteger count2 = new AtomicInteger(count);
        try (Transaction outerTransaction = Transaction.openOuter()) {
            BackpackWrapperLookup.get(inventoryStack).ifPresent(backpack -> {
                for (SCItemStackHandlerSlot storageViews : backpack.getInventoryHandler().getSlotsContaining(itemStack.getItem())) {
                    int result = (int) storageViews.extract(ItemVariant.of(itemStack), count, outerTransaction);
                    count2.addAndGet(-result);
                    if(count2.get() <= 0) break;
                }
            });
            outerTransaction.commit();
        } catch (Exception ignored) {}
        return count2.get();
    }
}
