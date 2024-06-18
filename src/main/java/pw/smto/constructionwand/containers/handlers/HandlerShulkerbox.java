package pw.smto.constructionwand.containers.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import pw.smto.constructionwand.api.IContainerHandler;
import pw.smto.constructionwand.basics.WandUtil;

public class HandlerShulkerbox implements IContainerHandler
{
    private final int SLOTS = 27;

    @Override
    public boolean matches(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        return inventoryStack != null && inventoryStack.getCount() == 1 && Block.getBlockFromItem(inventoryStack.getItem()) instanceof ShulkerBoxBlock;
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        int count = 0;

        for(ItemStack stack : getItemList(inventoryStack)) {
            if(WandUtil.stackEquals(stack, itemStack)) count += stack.getCount();
        }

        return count;
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        DefaultedList<ItemStack> itemList = getItemList(inventoryStack);
        boolean changed = false;

        for(ItemStack stack : itemList) {
            if(WandUtil.stackEquals(stack, itemStack)) {
                int toTake = Math.min(count, stack.getCount());
                stack.decrement(toTake);
                count -= toTake;
                changed = true;
                if(count == 0) break;
            }
        }
        if(changed) {
            setItemList(inventoryStack, itemList);
            player.getInventory().markDirty();
        }

        return count;
    }

    private DefaultedList<ItemStack> getItemList(ItemStack itemStack) {
        NbtCompound nbtCompound = itemStack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(SLOTS, ItemStack.EMPTY);
        if (nbtCompound != null) {
            if (nbtCompound.contains("Items", NbtElement.LIST_TYPE)) {
                Inventories.readNbt(nbtCompound, defaultedList, DynamicRegistryManager.EMPTY);
            }
        }
        return defaultedList;
    }

    private void setItemList(ItemStack itemStack, DefaultedList<ItemStack> itemStacks) {
        NbtCompound nbtCompound = itemStack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
        if (nbtCompound != null) {
            nbtCompound.remove("Items");
            Inventories.writeNbt(nbtCompound, itemStacks, DynamicRegistryManager.EMPTY);
            itemStack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbtCompound));
        }
    }
}
