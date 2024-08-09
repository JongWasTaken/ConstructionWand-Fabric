package pw.smto.constructionwand.items.wand;


import net.minecraft.item.Item;
import pw.smto.constructionwand.ConstructionWand;

public class ItemWandInfinity extends ItemWand
{
    public ItemWandInfinity() {
        super(new Item.Settings().maxCount(1).fireproof(), ConstructionWand.id("infinity_wand"));
    }
}
