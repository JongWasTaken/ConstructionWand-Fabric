package dev.smto.constructionwand.basics.option;

import dev.smto.constructionwand.api.IWandUpgrade;
import net.minecraft.nbt.CompoundTag;

public class WandUpgradesSelectable<T extends IWandUpgrade> extends WandUpgrades<T> implements IOption<T> {
    private byte selector;

    public WandUpgradesSelectable(CompoundTag tag, String key, T dval) {
        super(tag, key, dval);
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getValueString() {
        return this.get().getRegistryName().toString();
    }

    @Override
    public void setValueString(String val) {
        for (byte i = 0; i < this.upgrades.size(); i++) {
            if (this.upgrades.get(i).getRegistryName().toString().equals(val)) {
                this.selector = i;
                this.serializeSelector();
                return;
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return this.upgrades.size() > 1;
    }

    @Override
    public void set(T val) {
        this.selector = (byte) this.upgrades.indexOf(val);
        this.fixSelector();
        this.serializeSelector();
    }

    @Override
    public T get() {
        this.fixSelector();
        return this.upgrades.get(this.selector);
    }

    @Override
    public T next(boolean dir) {
        this.selector++;
        this.fixSelector();
        this.serializeSelector();
        return this.get();
    }

    private void fixSelector() {
        if (this.selector < 0 || this.selector >= this.upgrades.size()) this.selector = 0;
    }

    @Override
    protected void deserialize() {
        super.deserialize();

        this.selector = this.tag.getByte(this.key + "_sel").orElse((byte) 0);
        this.fixSelector();
    }

    @Override
    protected void serialize() {
        super.serialize();

        this.serializeSelector();
    }

    private void serializeSelector() {
        this.tag.putByte(this.key + "_sel", this.selector);
    }
}
