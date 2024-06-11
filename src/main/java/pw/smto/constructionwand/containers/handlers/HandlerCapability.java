package pw.smto.constructionwand.containers.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import pw.smto.constructionwand.api.IContainerHandler;
import pw.smto.constructionwand.basics.WandUtil;

public class HandlerCapability implements IContainerHandler
{
    @Override
    public boolean matches(PlayerEntity player, ItemStack target, ItemStack current) {
        // this is almost cavemanish compared to forge but its a best-effort type of thing
        NbtCompound nbtCompound = current.hasNbt() ? current.getNbt() : new NbtCompound();
        NbtList items = nbtCompound.contains("Items") ? nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE) : new NbtList();
        if (items.size() != 0) {
            return true;
        }
        return false;
    }

    @Override
    public int countItems(PlayerEntity player, ItemStack target, ItemStack current) {
        NbtCompound nbtCompound = current.hasNbt() ? current.getNbt() : new NbtCompound();
        NbtList items = nbtCompound.contains("Items") ? nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE) : new NbtList();
        if (items.size() != 0) {
            var inv = new SimpleInventory();
            inv.readNbtList(items);
            int total = 0;
            for(int i = 0; i < inv.size(); i++) {
                ItemStack containerStack = inv.getStack(i);
                if(WandUtil.stackEquals(target, containerStack)) {
                    total += Math.max(0, containerStack.getCount());
                }
            }
            return total;
        }
        return 0;
    }

    @Override
    public int useItems(PlayerEntity player, ItemStack target, ItemStack current, int count) {
        NbtCompound nbtCompound = current.hasNbt() ? current.getNbt() : new NbtCompound();
        NbtList items = nbtCompound.contains("Items") ? nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE) : new NbtList();
        if (items.size() != 0) {
            var inv = new SimpleInventory();
            inv.readNbtList(items);

            for(int i = 0; i < inv.size(); i++) {
                ItemStack handlerStack = inv.getStack(i);
                if(WandUtil.stackEquals(target, handlerStack)) {
                    ItemStack extracted = inv.removeItem(handlerStack.getItem(), count);
                    count -= extracted.getCount();
                    if(count <= 0) break;
                }
            }
            return count;
        }
        return 0;
    }
}
