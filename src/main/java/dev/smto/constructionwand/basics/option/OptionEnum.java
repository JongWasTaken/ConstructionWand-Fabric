package dev.smto.constructionwand.basics.option;

import com.google.common.base.Enums;
import net.minecraft.nbt.CompoundTag;

public class OptionEnum<E extends Enum<E>> implements IOption<E> {
    private final CompoundTag tag;
    private final String key;
    private final Class<E> enumClass;
    private final boolean enabled;
    private final E dval;
    private E value;

    public OptionEnum(CompoundTag tag, String key, Class<E> enumClass, E dval, boolean enabled) {
        this.tag = tag;
        this.key = key;
        this.enumClass = enumClass;
        this.enabled = enabled;
        this.dval = dval;

        this.value = Enums.getIfPresent(enumClass, tag.getString(key).orElse("").toUpperCase()).or(dval);
    }

    public OptionEnum(CompoundTag tag, String key, Class<E> enumClass, E dval) {
        this(tag, key, enumClass, dval, true);
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getValueString() {
        return this.value.name().toLowerCase();
    }

    @Override
    public void setValueString(String val) {
        this.set(Enums.getIfPresent(this.enumClass, val.toUpperCase()).or(this.dval));
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void set(E val) {
        if (!this.enabled) return;
        this.value = val;
        this.tag.putString(this.key, this.getValueString());
    }

    @Override
    public E get() {
        return this.value;
    }

    @Override
    public E next(boolean dir) {
        E[] enumValues = this.enumClass.getEnumConstants();
        int i = this.value.ordinal() + (dir ? 1 : -1);
        if (i < 0) i += enumValues.length;
        this.set(enumValues[i % enumValues.length]);
        return this.value;
    }
}
