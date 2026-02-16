package dev.smto.constructionwand.integrations.polymer.item;

import dev.smto.constructionwand.integrations.polymer.PolymerManager;
import dev.smto.constructionwand.items.core.CoreItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class PolymerCoreItem extends CoreItem implements PolymerItem, PolymerKeepModel, PolymerClientDecoded
{
    private final PolymerModelData model;

    public PolymerCoreItem(Identifier id, Settings properties) {
        super(properties);
        this.model = PolymerResourcePackUtils.requestModel(Items.STICK, id);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity serverPlayerEntity) {
        if (serverPlayerEntity == null) return Items.STICK;
        if (PolymerManager.hasClientMod(serverPlayerEntity)) {
            return this;
        }
        return this.model.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.value();
    }
}
