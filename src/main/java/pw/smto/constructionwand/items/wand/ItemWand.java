package pw.smto.constructionwand.items.wand;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
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

import java.util.function.Consumer;

public abstract class ItemWand extends Item
{
    public final RegistryKey<Item> registryKey;
    public ItemWand(RegistryKey<Item> id, Item.Settings properties) {
        super(properties.registryKey(id));
        this.registryKey = id;
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
    public ActionResult use(@NotNull World world, PlayerEntity player, @NotNull Hand hand) {
        ItemStack offHandStack = player.getOffHandStack();
        ItemStack wand = player.getStackInHand(hand);
        if (offHandStack.isEmpty()) return ActionResult.FAIL;
        if (wand.equals(offHandStack)) return ActionResult.FAIL;
        if(!player.isSneaking()) {
            if(world.isClient) return ActionResult.FAIL;

            // Right click: Place angel block
            WandJob job = getWandJob(player, world, BlockHitResult.createMissed(player.getEyePos(),
                    WandUtil.fromVector(player.getEyePos()), player.getBlockPos()), wand);
            ConstructionWand.LOGGER.warn("Job: {}", job);
            return job.doIt() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }
        return ActionResult.FAIL;
    }

    public static WandJob getWandJob(PlayerEntity player, World world, @Nullable BlockHitResult rayTraceResult, ItemStack wand) {
        WandJob wandJob = new WandJob(player, world, rayTraceResult, wand);
        wandJob.getSnapshots();

        return wandJob;
    }

    public int remainingDurability(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        WandOptions options = WandOptions.of(stack);
        int limit = options.cores.get().getWandAction().getLimit(stack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        // +SHIFT tooltip: show all options + installed cores
        if(Screen.hasShiftDown()) {
            for(int i = 1; i < options.allOptions.length; i++) {
                IOption<?> opt = options.allOptions[i];
                textConsumer.accept(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                        .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.GRAY))
                );
            }
            if(!options.cores.getUpgrades().isEmpty()) {
                textConsumer.accept(Text.literal(""));
                textConsumer.accept(Text.translatable(langTooltip + "cores").formatted(Formatting.GRAY));

                for(IWandCore core : options.cores.getUpgrades()) {
                    textConsumer.accept(Text.translatable(options.cores.getKeyTranslation() + "." + core.getRegistryName().toString()));
                }
            }
        }
        // Default tooltip: show block limit + active wand core
        else {
            IOption<?> opt = options.allOptions[0];
            textConsumer.accept(Text.translatable(langTooltip + "blocks", limit).formatted(Formatting.GRAY));
            textConsumer.accept(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                    .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.WHITE)));
            textConsumer.accept(Text.translatable(langTooltip + "shift").formatted(Formatting.AQUA));
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
