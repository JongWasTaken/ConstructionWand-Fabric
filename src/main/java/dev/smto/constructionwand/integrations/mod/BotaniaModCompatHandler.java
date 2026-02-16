package dev.smto.constructionwand.integrations.mod;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.api.IModCompatHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.item.BlockProvider;

import java.util.Optional;

public class BotaniaModCompatHandler implements IModCompatHandler, IContainerHandler
{
    @Override
    public boolean matches(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        return BotaniaFabricCapabilities.BLOCK_PROVIDER.find(inventoryStack, Unit.INSTANCE) != null;
        //return inventoryStack != null && inventoryStack.getCapability(BotaniaFabricCapabilities.BLOCK_PROVIDER).isPresent();
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        int provCount = 0;
        try {
            Optional<BlockProvider> provOptional = Optional.ofNullable(BotaniaFabricCapabilities.BLOCK_PROVIDER.find(inventoryStack, Unit.INSTANCE));
            if(provOptional.isEmpty()) return 0;

            BlockProvider prov = provOptional.get();
            provCount = prov.getBlockCount(player, inventoryStack, Block.getBlockFromItem(itemStack.getItem()));
            if(provCount == -1)
                return Integer.MAX_VALUE;
        } catch (Throwable ignored) {}
        return provCount;
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        try {
            Optional<BlockProvider> provOptional = Optional.ofNullable(BotaniaFabricCapabilities.BLOCK_PROVIDER.find(inventoryStack, Unit.INSTANCE));
            if(provOptional.isEmpty()) return 0;

            BlockProvider prov = provOptional.get();
            if(prov.provideBlock(player, inventoryStack, Block.getBlockFromItem(itemStack.getItem()), true))
                return 0;
        } catch (Throwable ignored) {}
        return count;
    }
}