package pw.smto.constructionwand.integrations.container;

import com.supermartijn642.packedup.BackpackInventory;
import com.supermartijn642.packedup.BackpackItem;
import com.supermartijn642.packedup.BackpackStorageManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IContainerHandler;
import pw.smto.constructionwand.basics.WandUtil;

public class HandlerPackedUpBackpack implements IContainerHandler {
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
        } catch (Exception ignored) {
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
        } catch (Exception ignored) {
            ConstructionWand.LOGGER.error("Failed to extract items from \"Packed up!\" backpack of player {}!", player.getGameProfile().getName());
        }
        return count;
    }
}
