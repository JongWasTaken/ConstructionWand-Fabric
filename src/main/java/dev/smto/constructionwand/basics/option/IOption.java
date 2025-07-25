package dev.smto.constructionwand.basics.option;

import dev.smto.constructionwand.ConstructionWand;

public interface IOption<T>
{
    String getKey();

    String getValueString();

    void setValueString(String val);

    default String getKeyTranslation() {
        return ConstructionWand.MOD_ID + ".option." + getKey();
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
