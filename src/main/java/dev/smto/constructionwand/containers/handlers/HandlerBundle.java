package dev.smto.constructionwand.containers.handlers;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class HandlerBundle implements IContainerHandler
{
    @Override
    public boolean matches(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        return inventoryStack != null && inventoryStack.getCount() == 1 && inventoryStack.getItem() == Items.BUNDLE;
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        return getContents(inventoryStack).filter((stack) -> WandUtil.stackEquals(stack, itemStack))
                .map(ItemStack::getCount).reduce(0, Integer::sum);
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        AtomicInteger newCount = new AtomicInteger(count);

        List<ItemStack> itemStacks = getContents(inventoryStack).filter((stack -> {
            if(WandUtil.stackEquals(stack, itemStack)) {
                int toTake = Math.min(newCount.get(), stack.getCount());
                stack.decrement(toTake);
                newCount.set(newCount.get() - toTake);
            }
            return !stack.isEmpty();
        })).toList();

        setItemList(inventoryStack, itemStacks);

        return newCount.get();
    }

    private Stream<ItemStack> getContents(ItemStack bundleStack) {
        NbtCompound compoundtag = bundleStack.getNbt();
        if(compoundtag == null) {
            return Stream.empty();
        }
        else {
            NbtList listtag = compoundtag.getList("Items", 10);
            return listtag.stream().map(NbtCompound.class::cast).map(ItemStack::fromNbt);
        }
    }

    private void setItemList(ItemStack itemStack, List<ItemStack> itemStacks) {
        NbtCompound rootTag = itemStack.getOrCreateNbt();
        NbtList listTag = new NbtList();
        rootTag.put("Items", listTag);

        for(ItemStack stack : itemStacks) {
            NbtCompound itemTag = new NbtCompound();
            stack.setNbt(itemTag);
            listTag.add(itemTag);
        }
    }
}
