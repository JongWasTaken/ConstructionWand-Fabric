package pw.smto.constructionwand.items.wand;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
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
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.api.IWandCore;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.IOption;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.wand.WandJob;
import pw.smto.constructionwand.wand.undo.UndoHistory;

import java.util.List;

import static pw.smto.constructionwand.ConstructionWand.id;

public abstract class ItemWand extends Item
{
    public ItemWand(Item.Settings properties) {
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
            WandJob job = getWandJob(player, world, new BlockHitResult(context.getHitPos(), context.getSide(), context.getBlockPos(), false), stack);
            return job.doIt() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }
    }

    @NotNull
    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, PlayerEntity player, @NotNull Hand hand) {
        ItemStack offHandStack = player.getOffHandStack();
        ItemStack wand = player.getStackInHand(hand);
        if (offHandStack.isEmpty()) return TypedActionResult.fail(wand);
        if (wand.equals(offHandStack)) return TypedActionResult.fail(wand);
        if(!player.isSneaking()) {
            if(world.isClient) return TypedActionResult.fail(wand);

            // Right click: Place angel block
            WandJob job = getWandJob(player, world, BlockHitResult.createMissed(player.getEyePos(),
                    WandUtil.fromVector(player.getEyePos()), player.getBlockPos()), wand);
            ConstructionWand.LOGGER.warn("Job: " + job);
            return job.doIt() ? TypedActionResult.success(wand) : TypedActionResult.fail(wand);
        }
        return TypedActionResult.fail(wand);
    }

    public static WandJob getWandJob(PlayerEntity player, World world, @Nullable BlockHitResult rayTraceResult, ItemStack wand) {
        WandJob wandJob = new WandJob(player, world, rayTraceResult, wand);
        wandJob.getSnapshots();

        return wandJob;
    }

    @Override
    public boolean isSuitableFor(@NotNull BlockState blockIn) {
        return false;
    }

    @Override
    public boolean canRepair(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return false;
    }

    public int remainingDurability(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(@NotNull ItemStack itemstack, World worldIn, @NotNull List<Text> lines, @NotNull TooltipContext extraInfo) {
        WandOptions options = new WandOptions(itemstack);
        int limit = options.cores.get().getWandAction().getLimit(itemstack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        // +SHIFT tooltip: show all options + installed cores
        if(Screen.hasShiftDown()) {
            for(int i = 1; i < options.allOptions.length; i++) {
                IOption<?> opt = options.allOptions[i];
                lines.add(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                        .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.GRAY))
                );
            }
            if(!options.cores.getUpgrades().isEmpty()) {
                lines.add(Text.literal(""));
                lines.add(Text.translatable(langTooltip + "cores").formatted(Formatting.GRAY));

                for(IWandCore core : options.cores.getUpgrades()) {
                    lines.add(Text.translatable(options.cores.getKeyTranslation() + "." + core.getRegistryName().toString()));
                }
            }
        }
        // Default tooltip: show block limit + active wand core
        else {
            IOption<?> opt = options.allOptions[0];
            lines.add(Text.translatable(langTooltip + "blocks", limit).formatted(Formatting.GRAY));
            lines.add(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                    .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.WHITE)));
            lines.add(Text.translatable(langTooltip + "shift").formatted(Formatting.AQUA));
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
