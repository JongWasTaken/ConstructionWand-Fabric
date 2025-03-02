package pw.smto.constructionwand.items.core;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandCore;
import pw.smto.constructionwand.wand.undo.PlayerInstance;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public abstract class ItemCore extends Item implements IWandCore, PolymerItem
{
    public final RegistryKey<Item> registryKey;

    public ItemCore(RegistryKey<Item> id, Item.Settings properties) {
        super(properties.registryKey(id));
        this.registryKey = id;

    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        var player = context.getPlayer();
        if (player == null) return Items.STICK;
        if (PlayerInstance.getEntryFromPlayerEntity(player).hasClientMod) {
            return this;
        }
        return Items.STICK;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.registryKey.getValue();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(
                Text.translatable(ConstructionWand.MOD_ID + ".option.cores." + getRegistryName().toString() + ".desc")
                        .formatted(Formatting.GRAY)
        );
        tooltip.add(
                Text.translatable(ConstructionWand.MOD_ID + ".tooltip.core_tip").formatted(Formatting.AQUA)
        );
    }
}
