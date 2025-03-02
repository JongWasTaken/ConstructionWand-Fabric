package pw.smto.constructionwand.items.wand;


import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;

public class ItemWandInfinity extends ItemWand
{
    public ItemWandInfinity(RegistryKey<Item> id) {
        super(id, new Item.Settings().maxCount(1).fireproof());
    }
}
