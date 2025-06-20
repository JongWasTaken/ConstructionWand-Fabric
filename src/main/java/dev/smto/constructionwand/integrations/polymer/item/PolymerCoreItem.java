package dev.smto.constructionwand.integrations.polymer.item;

import dev.smto.constructionwand.integrations.polymer.PolymerManager;
import dev.smto.constructionwand.items.core.CoreItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public abstract class PolymerCoreItem extends CoreItem implements PolymerItem, PolymerKeepModel, PolymerClientDecoded
{
    public PolymerCoreItem(RegistryKey<Item> id, Settings properties) {
        super(id, properties);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        var player = context.getPlayer();
        if (player == null) return Items.STICK;
        if (PolymerManager.hasClientMod(player)) {
            return this;
        }
        return Items.STICK;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.registryKey.getValue();
    }
}
