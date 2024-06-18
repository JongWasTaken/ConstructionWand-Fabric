package pw.smto.constructionwand.containers.handlers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import pw.smto.constructionwand.api.IContainerHandler;
import pw.smto.constructionwand.basics.WandUtil;

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
        NbtCompound compoundtag = bundleStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if(compoundtag == null) {
            return Stream.empty();
        }
        else {
            NbtList listtag = compoundtag.getList("Items", 10);
            return listtag.stream().map(NbtCompound.class::cast).map((e) -> {
                return ItemStack.fromNbt(DynamicRegistryManager.EMPTY, e).get();
            });
        }
    }

    private void setItemList(ItemStack itemStack, List<ItemStack> itemStacks) {
        NbtCompound rootTag = itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        NbtList listTag = new NbtList();
        rootTag.put("Items", listTag);

        for(ItemStack stack : itemStacks) {
            NbtCompound itemTag = new NbtCompound();
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(itemTag));
            listTag.add(itemTag);
        }
    }
}
