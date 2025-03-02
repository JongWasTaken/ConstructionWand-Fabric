package pw.smto.constructionwand.items.wand;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.wand.WandServerScreen;
import pw.smto.constructionwand.api.IWandCore;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.IOption;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.wand.WandJob;
import pw.smto.constructionwand.wand.undo.PlayerInstance;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public abstract class ItemWand extends Item implements PolymerItem, PolymerKeepModel, PolymerClientDecoded
{
    public final RegistryKey<Item> registryKey;
    private final Identifier model;
    public ItemWand(RegistryKey<Item> id, Item.Settings properties) {
        super(properties.registryKey(id));
        this.registryKey = id;
        this.model = Identifier.of(ConstructionWand.MOD_ID, id.getValue().getPath());
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

    @NotNull
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();

        if(world.isClient || player == null) return ActionResult.PASS;

        ItemStack stack = player.getStackInHand(hand);

        if(PlayerInstance.isUndoActive(player)) {
            PlayerInstance.getEntryFromPlayerEntity(player).blockServerWandScreen = true;
            var res = PlayerInstance.undo(player, world, context.getBlockPos()) ? ActionResult.SUCCESS : ActionResult.FAIL;
            return res;
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
        if (wand.equals(offHandStack)) return ActionResult.FAIL;
        if (world.isClient) return ActionResult.FAIL;
        if(!PlayerInstance.isUndoActive(player)) {
            if (offHandStack.isEmpty()) return ActionResult.FAIL;
            // Right click: Place angel block
            WandJob job = getWandJob(player, world, BlockHitResult.createMissed(player.getEyePos(),
                    WandUtil.fromVector(player.getEyePos()), player.getBlockPos()), wand);
            return job.doIt() ? ActionResult.SUCCESS : ActionResult.FAIL;
        } else {
            var i = PlayerInstance.getEntryFromPlayerEntity(player);
            if (!i.hasClientMod && !i.blockServerWandScreen) {
                WandServerScreen.open((ServerPlayerEntity) player, wand);
            } else i.blockServerWandScreen = false;
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

    @Override
    public void appendTooltip(ItemStack itemstack, TooltipContext context, List<Text> lines, TooltipType type) {
        WandOptions options = new WandOptions(itemstack);
        String langTooltip = ConstructionWand.MOD_ID + ".tooltip.";
        for(int i = 1; i < options.allOptions.length; i++) {
            IOption<?> opt = options.allOptions[i];
            if (opt.hasTranslation()) {
                lines.add(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                        .append(Text.translatable(opt.getValueTranslation()).formatted(Formatting.GRAY))
                );
            } else {
                lines.add(Text.translatable(opt.getKeyTranslation()).formatted(Formatting.AQUA)
                        .append(Text.literal(opt.getValueTranslation()).formatted(Formatting.GRAY))
                );
            }
        }
        if(!options.cores.getUpgrades().isEmpty()) {
            lines.add(Text.literal(""));
            lines.add(Text.translatable(langTooltip + "cores").formatted(Formatting.GRAY));

            for(IWandCore core : options.cores.getUpgrades()) {
                lines.add(Text.translatable(options.cores.getKeyTranslation() + "." + core.getRegistryName().toString()));
            }
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
