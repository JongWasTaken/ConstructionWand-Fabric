package pw.smto.constructionwand.items.wand;


import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;

public class InfinityWandItem extends WandItem
{
    public InfinityWandItem(RegistryKey<Item> id) {
        super(id, new Item.Settings().maxCount(1).fireproof());
    }
}
