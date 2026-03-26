package dev.smto.constructionwand.wand.supplier;

import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.basics.pool.RandomPool;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class SupplierRandom extends SupplierInventory
{
    public SupplierRandom(Player player, WandOptions options) {
        super(player, options);
    }

    @Override
    public void getSupply(@Nullable BlockItem target) {
        itemCounts = new LinkedHashMap<BlockItem, Integer>();

        // Random mode -> add all items from hotbar
        itemPool = new RandomPool<>(player.getRandom());

        for(ItemStack stack : WandUtil.getHotbarWithOffhand(player)) {
            if(stack.getItem() instanceof BlockItem) addBlockItem((BlockItem) stack.getItem());
        }
    }
}
