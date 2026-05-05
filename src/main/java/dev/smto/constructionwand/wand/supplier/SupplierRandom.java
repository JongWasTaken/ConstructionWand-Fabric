package dev.smto.constructionwand.wand.supplier;

import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.basics.pool.RandomPool;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

public class SupplierRandom extends SupplierInventory {
    public SupplierRandom(Player player, WandOptions options) {
        super(player, options);
    }

    @Override
    public void getSupply(@Nullable BlockItem target) {
        this.itemCounts = new LinkedHashMap<>();

        // Random mode -> add all items from hotbar
        this.itemPool = new RandomPool<>(this.player.getRandom());

        for (ItemStack stack : WandUtil.getHotbarWithOffhand(this.player)) {
            if (stack.getItem() instanceof BlockItem) this.addBlockItem((BlockItem) stack.getItem());
        }
    }
}
