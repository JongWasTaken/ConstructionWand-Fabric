package pw.smto.constructionwand.containers.handlers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import pw.smto.constructionwand.api.IContainerHandler;
import pw.smto.constructionwand.basics.WandUtil;

public class HandlerNBTInventory implements IContainerHandler
{
    @Override
    public boolean matches(PlayerEntity player, ItemStack target, ItemStack current) {
        NbtCompound nbtCompound = current.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        NbtList items = nbtCompound.contains("Items") ? nbtCompound.getList("Items").orElse(new NbtList()) : new NbtList();
        return !items.isEmpty();
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack target, ItemStack current) {
        NbtCompound nbtCompound = current.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        NbtList items = nbtCompound.contains("Items") ? nbtCompound.getList("Items").orElse(new NbtList()) : new NbtList();
        if (!items.isEmpty()) {
            var inv = new SimpleInventory();
            NbtReadView.create(ErrorReporter.EMPTY, DynamicRegistryManager.EMPTY, nbtCompound).getOptionalTypedListView("Inventory", ItemStack.CODEC).ifPresent(inv::readDataList);
            int total = 0;
            for(int i = 0; i < inv.size(); i++) {
                ItemStack containerStack = inv.getStack(i);
                if(WandUtil.stackEquals(target, containerStack)) {
                    total += Math.max(0, containerStack.getCount());
                }
            }
            return total;
        }
        return 0;
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack target, ItemStack current, int count) {
        NbtCompound nbtCompound = current.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        NbtList items = nbtCompound.contains("Items") ? nbtCompound.getList("Items").orElse(new NbtList()) : new NbtList();
        if (!items.isEmpty()) {
            var inv = new SimpleInventory();
            NbtReadView.create(ErrorReporter.EMPTY, DynamicRegistryManager.EMPTY, nbtCompound).getOptionalTypedListView("Inventory", ItemStack.CODEC).ifPresent(inv::readDataList);
            for(int i = 0; i < inv.size(); i++) {
                ItemStack handlerStack = inv.getStack(i);
                if(WandUtil.stackEquals(target, handlerStack)) {
                    ItemStack extracted = inv.removeItem(handlerStack.getItem(), count);
                    count -= extracted.getCount();
                    if(count <= 0) break;
                }
            }
            return count;
        }
        return 0;
    }
}
