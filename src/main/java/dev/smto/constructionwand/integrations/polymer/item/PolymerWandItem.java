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
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Consumer;

public abstract class PolymerWandItem extends WandItem implements PolymerItem, PolymerKeepModel, PolymerClientDecoded
{
    public PolymerWandItem(RegistryKey<Item> id, Settings properties) {
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

    @NotNull
    @Override
    public ActionResult use(@NotNull World world, PlayerEntity player, @NotNull Hand hand) {
        ItemStack offHandStack = player.getOffHandStack();
        ItemStack wand = player.getStackInHand(hand);
        if (wand.equals(offHandStack)) return ActionResult.FAIL;
        if (world.isClient()) return ActionResult.FAIL;
        if(!UndoHistory.isUndoActive(player)) {
            if (offHandStack.isEmpty()) return ActionResult.FAIL;
            var bhr = BlockHitResult.createMissed(player.getEyePos(),
                    WandUtil.fromVector(player.getEyePos()), player.getBlockPos());

            if (ModCompat.preventWandOnBlock(new ItemUsageContext(world, player, hand, wand, bhr))) {
                return ActionResult.PASS;
            }
            // Right click: Place angel block
            WandJob job = new WandJob(player, world, bhr, wand);
            return job.run() ? ActionResult.SUCCESS : ActionResult.FAIL;
        } else {
            if (!PolymerManager.hasClientMod(player) && !PolymerManager.isScreenBlocked(player)) {
                PolymerManager.openServerScreen((ServerPlayerEntity) player, wand);
            } else PolymerManager.unblockServerScreen(player);
        }
        return ActionResult.FAIL;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        WandOptions options = WandOptions.of(stack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        int limit = options.cores.get().getWandAction().getLimit(stack);
        textConsumer.accept(Text.translatable(langTooltip + "blocks", limit).formatted(Formatting.GRAY));
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
}
