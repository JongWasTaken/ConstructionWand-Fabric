package dev.smto.constructionwand.containers.handlers;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class HandlerContainerComponent implements IContainerHandler
{
    @Override
    public boolean matches(Player player, ItemStack target, ItemStack current) {
        return current.getComponents().has(DataComponents.CONTAINER);
    }

    @Override
    public int countItems(Player player, ItemStack target, ItemStack current) {
        ItemContainerContents container = current.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        var items = NonNullList.withSize((int) container.allItemsCopyStream().count(), ItemStack.EMPTY);
        container.copyInto(items);
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
    public int useItems(Player player, ItemStack target, ItemStack current, int count) {
        ItemContainerContents container = current.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        var items = NonNullList.withSize((int) container.allItemsCopyStream().count(), ItemStack.EMPTY);
        container.copyInto(items);
        if (!items.isEmpty()) {
            for(int i = 0; i < items.size(); i++) {
                ItemStack handlerStack = items.get(i);
                if(WandUtil.stackEquals(target, handlerStack)) {
                    int toTake = Math.min(count, handlerStack.getCount());
                    count -= toTake;
                    handlerStack.shrink(toTake);
                    if (handlerStack.getCount() == 0) items.set(i, ItemStack.EMPTY);
                    if(count <= 0) break;
                }
            }
            current.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        }
        return count;
    }
}
