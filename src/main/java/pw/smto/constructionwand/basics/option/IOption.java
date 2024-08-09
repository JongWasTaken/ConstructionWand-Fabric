package pw.smto.constructionwand.basics.option;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import pw.smto.constructionwand.ConstructionWand;

public interface IOption<T>
{
    String getKey();

    String getValueString();

    void setValueString(String val);

    default String getKeyTranslation() {
        return ConstructionWand.MOD_ID + ".option." + getKey();
    }

    default boolean hasTranslation() {
        return true;
    }

    default String getValueTranslation() {
        return ConstructionWand.MOD_ID + ".option." + getKey() + "." + getValueString();
    }

    default String getDescTranslation() {
        return ConstructionWand.MOD_ID + ".option." + getKey() + "." + getValueString() + ".desc";
    }

    boolean isEnabled();

    void set(T val);

    T get();

    T next(boolean dir);

    default T next() {
        return next(true);
    }
}
