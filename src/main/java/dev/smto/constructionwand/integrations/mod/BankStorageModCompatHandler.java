package dev.smto.constructionwand.integrations.mod;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.api.IModCompatHandler;
import dev.smto.constructionwand.basics.WandUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;

public class BankStorageModCompatHandler implements IModCompatHandler, IContainerHandler {
    @Override
    public boolean matches(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        try {
            return inventoryStack != null && inventoryStack.getCount() == 1 && inventoryStack.getItem() instanceof BankItem;
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        try {
            if(player.getWorld().isClient) {
                CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(inventoryStack);
                if(cachedBankStorage == null) return 0;

                return cachedBankStorage.blockItems.stream().filter(stack -> WandUtil.stackEquals(stack, itemStack)).map(ItemStack::getCount).reduce(0, Integer::sum);
            } else {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(inventoryStack, player.getWorld());
                if(bankItemStorage == null) return 0;

                return bankItemStorage.getBlockItems().stream().filter(stack -> WandUtil.stackEquals(stack, itemStack)).map(ItemStack::getCount).reduce(0, Integer::sum);
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        try {
            BankItemStorage bankItemStorage = Util.getBankItemStorage(inventoryStack, player.getWorld());
            if(bankItemStorage == null) return 0;

            for(int i = 0; i < bankItemStorage.getItems().size(); i++) {
                ItemStack handlerStack = bankItemStorage.getItems().get(i);
                if(WandUtil.stackEquals(itemStack, handlerStack)) {
                    ItemStack extracted = bankItemStorage.removeStack(i, count);
                    count -= extracted.getCount();
                    if(count <= 0) break;
                }
            }

            if(!player.getWorld().isClient) {
                NetworkUtil.syncCachedBankS2C(bankItemStorage.uuid, (ServerPlayerEntity) player);
            }
        } catch (Throwable ignored) {}
        return count;
    }
}
