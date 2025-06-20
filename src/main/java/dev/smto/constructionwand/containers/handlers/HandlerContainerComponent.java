package dev.smto.constructionwand.containers.handlers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.basics.WandUtil;

public class HandlerContainerComponent implements IContainerHandler
{
    @Override
    public boolean matches(PlayerEntity player, ItemStack target, ItemStack current) {
        return current.getComponents().contains(DataComponentTypes.CONTAINER);
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack target, ItemStack current) {
        ContainerComponent container = current.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        var items = DefaultedList.ofSize((int) container.stream().count(), ItemStack.EMPTY);
        container.copyTo(items);
        if (!items.isEmpty()) {
            int total = 0;
            for (ItemStack containerStack : items) {
                if (WandUtil.stackEquals(target, containerStack)) {
                    total += Math.max(0, containerStack.getCount());
                }
            }
            return total;
        }
        return 0;
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack target, ItemStack current, int count) {
        ContainerComponent container = current.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        var items = DefaultedList.ofSize((int) container.stream().count(), ItemStack.EMPTY);
        container.copyTo(items);
        if (!items.isEmpty()) {
            for(int i = 0; i < items.size(); i++) {
                ItemStack handlerStack = items.get(i);
                if(WandUtil.stackEquals(target, handlerStack)) {
                    int toTake = Math.min(count, handlerStack.getCount());
                    count -= toTake;
                    handlerStack.decrement(toTake);
                    if (handlerStack.getCount() == 0) items.set(i, ItemStack.EMPTY);
                    if(count <= 0) break;
                }
            }
            current.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(items));
        }
        return count;
    }
}
