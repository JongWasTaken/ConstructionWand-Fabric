package dev.smto.constructionwand.integrations.polymer.item;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandCore;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.IOption;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.integrations.mod.ModCompat;
import dev.smto.constructionwand.integrations.polymer.PolymerManager;
import dev.smto.constructionwand.items.wand.WandItem;
import dev.smto.constructionwand.wand.WandJob;
import dev.smto.constructionwand.wand.undo.UndoHistory;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public abstract class PolymerWandItem extends WandItem implements PolymerItem, PolymerClientDecoded
{
    public PolymerWandItem(ResourceKey<Item> id, Properties properties) {
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

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();

        if(world.isClientSide() || player == null) return InteractionResult.PASS;

        if (ModCompat.preventWandOnBlock(context)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);

        if(UndoHistory.isUndoActive(player)) {
            PolymerManager.blockServerScreen(player);
            return UndoHistory.undo(player, world, context.getClickedPos()) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        else {
            WandJob job = new WandJob(player, world, new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), false), stack);
            return job.run() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack offHandStack = player.getOffhandItem();
        ItemStack wand = player.getItemInHand(hand);
        if (wand.equals(offHandStack)) return InteractionResult.FAIL;
        if (world.isClientSide()) return InteractionResult.FAIL;
        if(!UndoHistory.isUndoActive(player)) {
            if (offHandStack.isEmpty()) return InteractionResult.FAIL;
            var bhr = BlockHitResult.miss(player.getEyePosition(),
                    WandUtil.fromVector(player.getEyePosition()), player.blockPosition());

            if (ModCompat.preventWandOnBlock(new UseOnContext(world, player, hand, wand, bhr))) {
                return InteractionResult.PASS;
            }
            // Right click: Place angel block
            WandJob job = new WandJob(player, world, bhr, wand);
            return job.run() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        } else {
            if (!PolymerManager.hasClientMod(player.getUUID()) && !PolymerManager.isScreenBlocked(player)) {
                PolymerManager.openServerScreen((ServerPlayer) player, wand);
            } else PolymerManager.unblockServerScreen(player);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        WandOptions options = WandOptions.of(stack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        int limit = options.cores.get().getWandAction().getLimit(stack);
        textConsumer.accept(Component.translatable(langTooltip + "blocks", limit).withStyle(ChatFormatting.GRAY));
        for(int i = 1; i < options.allOptions.length; i++) {
            IOption<?> opt = options.allOptions[i];
            textConsumer.accept(Component.translatable(opt.getKeyTranslation()).withStyle(ChatFormatting.AQUA)
                    .append(Component.translatable(opt.getValueTranslation()).withStyle(ChatFormatting.GRAY))
            );
        }
        if(!options.cores.getUpgrades().isEmpty()) {
            textConsumer.accept(Component.literal(""));
            textConsumer.accept(Component.translatable(langTooltip + "cores").withStyle(ChatFormatting.GRAY));

            for(IWandCore core : options.cores.getUpgrades()) {
                textConsumer.accept(Component.translatable(options.cores.getKeyTranslation() + "." + core.getRegistryName().toString()));
            }
        }
    }
}
