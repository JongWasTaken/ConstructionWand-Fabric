package pw.smto.constructionwand.basics.option;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import pw.smto.constructionwand.ConstructionWand;

public class OptionInt implements IOption<Integer> {
    private final NbtCompound tag;
    private final String key;
    private final boolean enabled;
    private Integer value;

    public OptionInt(NbtCompound tag, String key, Integer dval, boolean enabled) {
        this.tag = tag;
        this.key = key;
        this.enabled = enabled;

        if(tag.contains(key)) value = tag.getInt(key);
        else value = dval;
    }

    public OptionInt(NbtCompound tag, String key, Integer dval) {
        this(tag, key, dval, true);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean hasTranslation() {
        return false;
    }

    @Override
    public String getValueTranslation() {
        return getValueString() + "x";
    }

    @Override
    public String getValueString() {
        return String.valueOf(value);
    }

    @Override
    public String getDescTranslation() {
        return ConstructionWand.MOD_ID + ".option." + getKey() + ".desc";
    }

    @Override
    public void setValueString(String val) {
        try {
            set(Integer.parseInt(val));
        } catch (Exception ignored) {}
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void set(Integer val) {
        if(!enabled) return;
        value = val;
        tag.putInt(key, value);
    }

    @Override
    public Integer get() {
        return value;
    }

    @Override
    public Integer next(boolean unused) {
        set(value + 1);
        return value;
    }
}
