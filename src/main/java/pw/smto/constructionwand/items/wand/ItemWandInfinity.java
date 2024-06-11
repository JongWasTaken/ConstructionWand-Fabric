package pw.smto.constructionwand.items.wand;


public class ItemWandInfinity extends ItemWand
{
    public ItemWandInfinity(Settings properties) {
        super(properties.maxCount(1).fireproof());
    }
}
