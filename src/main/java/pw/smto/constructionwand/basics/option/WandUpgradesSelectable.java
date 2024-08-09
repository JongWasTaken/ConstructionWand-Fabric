package pw.smto.constructionwand.basics.option;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import pw.smto.constructionwand.Registry;
import pw.smto.constructionwand.api.IWandCore;
import pw.smto.constructionwand.api.IWandUpgrade;
import pw.smto.constructionwand.items.core.ItemCoreAngel;
import pw.smto.constructionwand.items.core.ItemCoreDestruction;

public class WandUpgradesSelectable extends WandUpgrades<pw.smto.constructionwand.api.IWandCore> implements IOption<pw.smto.constructionwand.api.IWandCore>
{
    private byte selector;

    public WandUpgradesSelectable(NbtCompound tag, String key, pw.smto.constructionwand.api.IWandCore dval) {
        super(tag, key, dval);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValueString() {
        return get().getRegistryName().toString();
    }

    @Override
    public void setValueString(String val) {
        for(byte i = 0; i < upgrades.size(); i++) {
            if(upgrades.get(i).getRegistryName().toString().equals(val)) {
                selector = i;
                serializeSelector();
                return;
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return upgrades.size() > 1;
    }

    @Override
    public void set(pw.smto.constructionwand.api.IWandCore val) {
        selector = (byte) upgrades.indexOf(val);
        fixSelector();
        serializeSelector();
    }

    @Override
    public pw.smto.constructionwand.api.IWandCore get() {
        fixSelector();
        return upgrades.get(selector);
    }

    @Override
    public pw.smto.constructionwand.api.IWandCore next(boolean dir) {
        if (dir) selector--; else selector++;
        if (selector < 0) selector = (byte) (upgrades.size() - 1);
        if (selector >= upgrades.size()) selector = 0;
        serializeSelector();
        return get();
    }

    private void fixSelector() {
        if(selector < 0 || selector >= upgrades.size()) selector = 0;
    }

    @Override
    protected void deserialize() {
        super.deserialize();

        selector = tag.getByte(key + "_sel");
        fixSelector();
    }

    @Override
    protected void serialize() {
        super.serialize();

        serializeSelector();
    }

    private void serializeSelector() {
        tag.putByte(key + "_sel", selector);
    }
}
