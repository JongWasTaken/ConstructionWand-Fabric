package dev.smto.constructionwand.containers.handlers;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;

import java.util.ArrayList;

public class HandlerBundleComponent implements IContainerHandler
{
    @Override
    public boolean matches(Player player, ItemStack target, ItemStack current) {
        return current.getComponents().has(DataComponents.BUNDLE_CONTENTS);
    }

    @Override
    public int countItems(Player player, ItemStack target, ItemStack current) {
        BundleContents bundle = current.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        if (!bundle.isEmpty()) {
            int total = 0;
            for (ItemStack containerStack : bundle.itemCopyStream().toList()) {
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
        var items = new ArrayList<>(current.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).itemCopyStream().toList());
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
            if (items.stream().allMatch(ItemStack::isEmpty)) {
                current.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            } else current.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items.stream().map(ItemStackTemplate::fromNonEmptyStack).toList()));
        }
        return count;
    }
}
