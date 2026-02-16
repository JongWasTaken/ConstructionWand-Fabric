package dev.smto.constructionwand.integrations.mod;

import com.supermartijn642.packedup.BackpackInventory;
import com.supermartijn642.packedup.BackpackItem;
import com.supermartijn642.packedup.BackpackStorageManager;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.api.IModCompatHandler;
import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class PackedUpModCompatHandler implements IModCompatHandler, IContainerHandler {
    @Override
    public boolean matches(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        return inventoryStack != null && inventoryStack.getCount() == 1 && inventoryStack.getItem() instanceof BackpackItem;
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        NbtCompound compound = inventoryStack.getOrCreateNbt();
        if(!compound.contains("packedup:invIndex") || BackpackStorageManager.getInventory(compound.getInt("packedup:invIndex")) == null){
            return 0;
        }
        try {
            int inventoryIndex = compound.getInt("packedup:invIndex");
            BackpackInventory inventory = BackpackStorageManager.getInventory(inventoryIndex);
            if (inventory == null) return 0;
            return inventory.getStacks().stream().filter(stack -> WandUtil.stackEquals(stack, itemStack)).map(ItemStack::getCount).reduce(0, Integer::sum);
        } catch (Throwable ignored) {
            ConstructionWand.LOGGER.error("Failed to count items in \"Packed up!\" backpack of player {}!", player.getGameProfile().getName());
            return 0;
        }
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        NbtCompound compound = inventoryStack.getOrCreateNbt();
        if(!compound.contains("packedup:invIndex") || BackpackStorageManager.getInventory(compound.getInt("packedup:invIndex")) == null){
            return 0;
        }
        try {
            int inventoryIndex = compound.getInt("packedup:invIndex");
            BackpackInventory inventory = BackpackStorageManager.getInventory(inventoryIndex);
            if (inventory == null) return 0;
            for(int i = 0; i < inventory.getStacks().size(); i++) {
                ItemStack handlerStack = inventory.getStacks().get(i);
                if(WandUtil.stackEquals(itemStack, handlerStack)) {
                    ItemStack extracted = inventory.extractItem(i, count);
                    count -= extracted.getCount();
                    if(count <= 0) break;
                }
            }
            BackpackStorageManager.save();
        } catch (Throwable ignored) {
            ConstructionWand.LOGGER.error("Failed to extract items from \"Packed up!\" backpack of player {}!", player.getGameProfile().getName());
        }
        return count;
    }
}
