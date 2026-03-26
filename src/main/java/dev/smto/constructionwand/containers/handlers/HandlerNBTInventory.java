package dev.smto.constructionwand.containers.handlers;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueInput;

public class HandlerNBTInventory implements IContainerHandler
{
    @Override
    public boolean matches(Player player, ItemStack target, ItemStack current) {
        CompoundTag nbtCompound = current.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag items = nbtCompound.contains("Items") ? nbtCompound.getList("Items").orElse(new ListTag()) : new ListTag();
        return !items.isEmpty();
    }

    @Override
    public int countItems(Player player, ItemStack target, ItemStack current) {
        CompoundTag nbtCompound = current.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag items = nbtCompound.contains("Items") ? nbtCompound.getList("Items").orElse(new ListTag()) : new ListTag();
        if (!items.isEmpty()) {
            var inv = new SimpleContainer();
            TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, nbtCompound).list("Inventory", ItemStack.CODEC).ifPresent(inv::fromItemList);
            int total = 0;
            for(int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack containerStack = inv.getItem(i);
                if(WandUtil.stackEquals(target, containerStack)) {
                    total += Math.max(0, containerStack.getCount());
                }
            }
            return total;
        }
        return 0;
    }

    @Override
    public int useItems(Player player, ItemStack target, ItemStack current, int count) {
        CompoundTag nbtCompound = current.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag items = nbtCompound.contains("Items") ? nbtCompound.getList("Items").orElse(new ListTag()) : new ListTag();
        if (!items.isEmpty()) {
            var inv = new SimpleContainer();
            TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, nbtCompound).list("Inventory", ItemStack.CODEC).ifPresent(inv::fromItemList);
            for(int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack handlerStack = inv.getItem(i);
                if(WandUtil.stackEquals(target, handlerStack)) {
                    ItemStack extracted = inv.removeItemType(handlerStack.getItem(), count);
                    count -= extracted.getCount();
                    if(count <= 0) break;
                }
            }
            return count;
        }
        return 0;
    }
}
