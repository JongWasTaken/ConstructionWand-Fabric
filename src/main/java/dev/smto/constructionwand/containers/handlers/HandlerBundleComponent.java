package dev.smto.constructionwand.containers.handlers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.basics.WandUtil;

public class HandlerBundleComponent implements IContainerHandler
{
    @Override
    public boolean matches(PlayerEntity player, ItemStack target, ItemStack current) {
        return current.getComponents().contains(DataComponentTypes.BUNDLE_CONTENTS);
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack target, ItemStack current) {
        BundleContentsComponent bundle = current.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        if (!bundle.isEmpty()) {
            int total = 0;
            for (ItemStack containerStack : bundle.iterate()) {
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
        var items = new java.util.ArrayList<>(current.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT).stream().toList());
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
            if (items.stream().allMatch(ItemStack::isEmpty)) {
                current.set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
            } else current.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(items));
        }
        return count;
    }
}
