package pw.smto.constructionwand.containers.handlers;

import com.supermartijn642.packedup.BackpackInventory;
import com.supermartijn642.packedup.BackpackItem;
import com.supermartijn642.packedup.BackpackStorageManager;
import com.supermartijn642.packedup.PackedUp;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;
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
        int inventoryIndex = compound.getInt("packedup:invIndex");
        BackpackInventory inventory = BackpackStorageManager.getInventory(inventoryIndex);
        if (inventory == null) return 0;
        return inventory.getStacks().stream().filter(stack -> WandUtil.stackEquals(stack, itemStack)).map(ItemStack::getCount).reduce(0, Integer::sum);
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        NbtCompound compound = inventoryStack.getOrCreateNbt();
        if(!compound.contains("packedup:invIndex") || BackpackStorageManager.getInventory(compound.getInt("packedup:invIndex")) == null){
            return 0;
        }
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
        return count;
    }
}
