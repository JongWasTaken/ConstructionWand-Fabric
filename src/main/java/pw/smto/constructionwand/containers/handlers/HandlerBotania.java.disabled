package pw.smto.constructionwand.containers.handlers;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;
import pw.smto.constructionwand.api.IContainerHandler;
import vazkii.botania.api.BotaniaFabricCapabilities;
import vazkii.botania.api.item.BlockProvider;

import java.util.Optional;

public class HandlerBotania implements IContainerHandler
{
    @Override
    public boolean matches(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        return BotaniaFabricCapabilities.BLOCK_PROVIDER.find(inventoryStack, Unit.INSTANCE) != null;
        //return inventoryStack != null && inventoryStack.getCapability(BotaniaFabricCapabilities.BLOCK_PROVIDER).isPresent();
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        Optional<BlockProvider> provOptional = Optional.ofNullable(BotaniaFabricCapabilities.BLOCK_PROVIDER.find(inventoryStack, Unit.INSTANCE));
        if(provOptional.isEmpty()) return 0;

        BlockProvider prov = provOptional.get();
        int provCount = prov.getBlockCount(player, inventoryStack, Block.getBlockFromItem(itemStack.getItem()));
        if(provCount == -1)
            return Integer.MAX_VALUE;
        return provCount;
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {

        Optional<BlockProvider> provOptional = Optional.ofNullable(BotaniaFabricCapabilities.BLOCK_PROVIDER.find(inventoryStack, Unit.INSTANCE));
        if(provOptional.isEmpty()) return 0;

        BlockProvider prov = provOptional.get();
        if(prov.provideBlock(player, inventoryStack, Block.getBlockFromItem(itemStack.getItem()), true))
            return 0;
        return count;
    }
}