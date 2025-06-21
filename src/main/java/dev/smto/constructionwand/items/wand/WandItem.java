package dev.smto.constructionwand.items.wand;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IWandCore;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.IOption;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.wand.WandJob;
import dev.smto.constructionwand.wand.undo.UndoHistory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class WandItem extends Item
{
    public WandItem(Item.Settings properties) {
        super(properties);
    }

    @NotNull
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();

        if(world.isClient || player == null) return ActionResult.PASS;

        ItemStack stack = player.getStackInHand(hand);

        if(player.isSneaking() && UndoHistory.isUndoActive(player)) {
            return UndoHistory.undo(player, world, context.getBlockPos()) ? ActionResult.SUCCESS : ActionResult.FAIL;
        }
        else {
            WandJob job = new WandJob(player, world, new BlockHitResult(context.getHitPos(), context.getSide(), context.getBlockPos(), false), stack);
            return job.run() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, PlayerEntity player, @NotNull Hand hand) {
        ItemStack offHandStack = player.getOffHandStack();
        ItemStack wand = player.getStackInHand(hand);
        if (offHandStack.isEmpty()) return TypedActionResult.fail(wand);
        if (wand.equals(offHandStack)) return TypedActionResult.fail(wand);
        if(!player.isSneaking()) {
            if(world.isClient) return TypedActionResult.fail(wand);

            // Right click: Place angel block
            WandJob job = new WandJob(player, world, BlockHitResult.createMissed(player.getEyePos(),
                    WandUtil.fromVector(player.getEyePos()), player.getBlockPos()), wand);
            //ConstructionWand.LOGGER.warn("Job: {}", job);
            return job.run() ? TypedActionResult.success(wand) : TypedActionResult.fail(wand);
        }
        return TypedActionResult.fail(wand);
    }

    public int remainingDurability(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        WandOptions options = WandOptions.of(stack);
        int limit = options.cores.get().getWandAction().getLimit(stack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        // +SHIFT tooltip: show all options + installed cores
        if(Screen.hasShiftDown()) {
            for(int i = 1; i < options.allOptions.length; i++) {
                IOption<?> opt = options.allOptions[i];
                tooltip.add(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                        .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.GRAY))
                );
            }
            if(!options.cores.getUpgrades().isEmpty()) {
                tooltip.add(Text.literal(""));
                tooltip.add(Text.translatable(langTooltip + "cores").formatted(Formatting.GRAY));

                for(IWandCore core : options.cores.getUpgrades()) {
                    tooltip.add(Text.translatable(options.cores.getKeyTranslation() + "." + core.getRegistryName().toString()));
                }
            }
        }
        // Default tooltip: show block limit + active wand core
        else {
            IOption<?> opt = options.allOptions[0];
            tooltip.add(Text.translatable(langTooltip + "blocks", limit).formatted(Formatting.GRAY));
            tooltip.add(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                    .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.WHITE)));
            tooltip.add(Text.translatable(langTooltip + "shift").formatted(Formatting.AQUA));
        }
    }

    public static void optionMessage(PlayerEntity player, IOption<?> option) {
        player.sendMessage(
                        Text.translatable(option.getKeyTranslation()).formatted(Formatting.AQUA)
                        .append(Text.translatable(option.getValueTranslation()).formatted(Formatting.WHITE))
                        .append(Text.literal(" - ").formatted(Formatting.GRAY))
                        .append(Text.translatable(option.getDescTranslation()).formatted(Formatting.WHITE))
                , true);
    }
}
