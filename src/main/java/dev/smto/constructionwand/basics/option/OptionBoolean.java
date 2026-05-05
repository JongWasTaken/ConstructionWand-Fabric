package dev.smto.constructionwand.basics.option;

import net.minecraft.nbt.CompoundTag;

public class OptionBoolean implements IOption<Boolean> {
    private final CompoundTag tag;
    private final String key;
    private final boolean enabled;
    private boolean value;

    public OptionBoolean(CompoundTag tag, String key, boolean dval, boolean enabled) {
        this.tag = tag;
        this.key = key;
        this.enabled = enabled;

        if (tag.contains(key)) this.value = tag.getBoolean(key).orElse(false);
        else this.value = dval;
    }

    public OptionBoolean(CompoundTag tag, String key, boolean dval) {
        this(tag, key, dval, true);
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getValueString() {
        return this.value ? "yes" : "no";
    }

    @Override
    public void setValueString(String val) {
        this.set(val.equals("yes"));
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void set(Boolean val) {
        if (!this.enabled) return;
        this.value = val;
        this.tag.putBoolean(this.key, this.value);
    }

    @Override
    public Boolean get() {
        return this.value;
    }

    @Override
    public Boolean next(boolean dir) {
        this.set(!this.value);
        return this.value;
    }
}
