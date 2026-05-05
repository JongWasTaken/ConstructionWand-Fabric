package dev.smto.constructionwand.integrations.polymer.item;

import dev.smto.constructionwand.integrations.polymer.PolymerManager;
import dev.smto.constructionwand.items.core.CoreItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public abstract class PolymerCoreItem extends CoreItem implements PolymerItem, PolymerClientDecoded {
    protected PolymerCoreItem(ResourceKey<Item> id, Properties properties) {
        super(id, properties);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        var playerProfile = (context.get(PacketContext.GAME_PROFILE));
        if (playerProfile == null) return Items.STICK;
        if (PolymerManager.hasClientMod(playerProfile.id())) {
            return this;
        }
        return Items.STICK;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return this.registryKey.identifier();
    }
}
