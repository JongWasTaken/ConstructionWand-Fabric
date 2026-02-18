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
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PolymerWandItem extends WandItem implements PolymerItem, PolymerKeepModel, PolymerClientDecoded
{
    private final PolymerModelData model;
    public PolymerWandItem(Identifier id, Settings properties) {
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

    @NotNull
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();

        if(world.isClient() || player == null) return ActionResult.PASS;

        if (ModCompat.preventWandOnBlock(context)) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);

        if(UndoHistory.isUndoActive(player)) {
            PolymerManager.blockServerScreen(player);
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
        if (wand.equals(offHandStack)) return TypedActionResult.fail(wand);
        if (world.isClient()) return TypedActionResult.fail(wand);
        if(!UndoHistory.isUndoActive(player)) {
            if (offHandStack.isEmpty()) return TypedActionResult.fail(wand);
            var bhr = BlockHitResult.createMissed(player.getEyePos(),
                    WandUtil.fromVector(player.getEyePos()), player.getBlockPos());

            if (ModCompat.preventWandOnBlock(new ItemUsageContext(world, player, hand, wand, bhr))) {
                return TypedActionResult.pass(wand);
            }
            // Right click: Place angel block
            WandJob job = new WandJob(player, world, bhr, wand);
            return job.run() ? TypedActionResult.success(wand) : TypedActionResult.fail(wand);
        } else {
            if (!PolymerManager.hasClientMod(player) && !PolymerManager.isScreenBlocked(player)) {
                PolymerManager.openServerScreen((ServerPlayerEntity) player, wand);
            } else PolymerManager.unblockServerScreen(player);
        }
        return TypedActionResult.fail(wand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        WandOptions options = WandOptions.of(stack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        int limit = options.cores.get().getWandAction().getLimit(stack);
        tooltip.add(Text.translatable(langTooltip + "blocks", limit).formatted(Formatting.GRAY));
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
}
