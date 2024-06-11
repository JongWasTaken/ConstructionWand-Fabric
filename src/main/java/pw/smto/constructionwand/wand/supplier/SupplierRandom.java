package pw.smto.constructionwand.wand.supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.basics.pool.RandomPool;

import java.util.LinkedHashMap;

public class SupplierRandom extends SupplierInventory
{
    public SupplierRandom(PlayerEntity player, WandOptions options) {
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
