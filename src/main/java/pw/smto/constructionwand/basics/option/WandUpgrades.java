package pw.smto.constructionwand.basics.option;

import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import pw.smto.constructionwand.api.IWandUpgrade;
import pw.smto.constructionwand.ConstructionWand;

import java.util.ArrayList;

public class WandUpgrades<T extends IWandUpgrade>
{
    protected final NbtCompound tag;
    protected final String key;
    protected final ArrayList<T> upgrades;
    protected final T dval;

    public WandUpgrades(NbtCompound tag, String key, T dval) {
        this.tag = tag;
        this.key = key;
        this.dval = dval;

        upgrades = new ArrayList<>();
        if(dval != null) upgrades.add(0, dval);

        deserialize();
    }

    protected void deserialize() {
        NbtList listnbt = tag.getList(key, NbtElement.STRING_TYPE);
        boolean require_fix = false;

        for(int i = 0; i < listnbt.size(); i++) {
            String str = listnbt.getString(i);
            Item item = Registries.ITEM.get(new Identifier(str));

            T data;
            try {
                //noinspection unchecked
                data = (T) item;
                upgrades.add(data);
            } catch(ClassCastException e) {
                ConstructionWand.LOGGER.warn("Invalid wand upgrade: " + str);
                require_fix = true;
            }
        }
        if(require_fix) serialize();
    }

    protected void serialize() {
        NbtList listnbt = new NbtList();

        for(T item : upgrades) {
            if(item == dval) continue;
            listnbt.add(NbtString.of(item.getRegistryName().toString()));
        }
        tag.put(key, listnbt);
    }

    public boolean addUpgrade(T upgrade) {
        if(hasUpgrade(upgrade)) return false;

        upgrades.add(upgrade);
        serialize();
        return true;
    }

    public boolean hasUpgrade(T upgrade) {
        return upgrades.contains(upgrade);
    }

    public ArrayList<T> getUpgrades() {
        return upgrades;
    }
}
