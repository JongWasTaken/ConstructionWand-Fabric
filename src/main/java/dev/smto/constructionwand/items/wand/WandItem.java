package dev.smto.constructionwand.items.wand;

import com.mojang.blaze3d.platform.InputConstants;
import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandCore;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.IOption;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.integrations.mod.ModCompat;
import dev.smto.constructionwand.wand.WandJob;
import dev.smto.constructionwand.wand.undo.UndoHistory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class WandItem extends Item
{
    public final ResourceKey<Item> registryKey;
    public WandItem(ResourceKey<Item> id, Item.Properties properties) {
        super(properties.setId(id));
        this.registryKey = id;
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

        if(player.isShiftKeyDown() && UndoHistory.isUndoActive(player)) {
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
        if (offHandStack.isEmpty()) return InteractionResult.FAIL;
        if (wand.equals(offHandStack)) return InteractionResult.FAIL;
        if(!player.isShiftKeyDown()) {
            if(world.isClientSide()) return InteractionResult.FAIL;

            var bhr = BlockHitResult.miss(player.getEyePosition(),
                    WandUtil.fromVector(player.getEyePosition()), player.blockPosition());

            if (ModCompat.preventWandOnBlock(new UseOnContext(world, player, hand, wand, bhr))) {
                return InteractionResult.PASS;
            }

            // Right click: Place angel block
            WandJob job = new WandJob(player, world, bhr, wand);
            //ConstructionWand.LOGGER.warn("Job: {}", job);
            return job.run() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }

    public int remainingDurability(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        WandOptions options = WandOptions.of(stack);
        int limit = options.cores.get().getWandAction().getLimit(stack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        // +SHIFT tooltip: show all options + installed cores
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT)) {
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
        // Default tooltip: show block limit + active wand core
        else {
            IOption<?> opt = options.allOptions[0];
            textConsumer.accept(Component.translatable(langTooltip + "blocks", limit).withStyle(ChatFormatting.GRAY));
            textConsumer.accept(Component.translatable(opt.getKeyTranslation()).withStyle(ChatFormatting.AQUA)
                    .append(Component.translatable(opt.getValueTranslation()).withStyle(ChatFormatting.WHITE)));
            textConsumer.accept(Component.translatable(langTooltip + "shift").withStyle(ChatFormatting.AQUA));
        }

    }

    public static void optionMessage(Player player, IOption<?> option) {
        player.sendOverlayMessage(
                        Component.translatable(option.getKeyTranslation()).withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable(option.getValueTranslation()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                        .append(Component.translatable(option.getDescTranslation()).withStyle(ChatFormatting.WHITE))
                );
    }
}
