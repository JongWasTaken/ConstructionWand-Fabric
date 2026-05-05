package dev.smto.constructionwand.integrations.mod;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IModCompatHandler;
import dev.smto.constructionwand.items.wand.WandItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class OpenPACModCompatHandler implements IModCompatHandler {
    // https://thexaero.github.io/open-parties-and-claims/javadoc/xaero/pac/common/server/claims/protection/api/IChunkProtectionAPI.html
    /*
    public boolean preventWandUseOnBlock(ItemUsageContext context) {
        if (context.getWorld() instanceof ServerWorld world) {
            return OpenPACServerAPI.get(world.getServer()).getChunkProtection()
                    .onBlockInteraction(context.getPlayer(), context.getHand(), context.getStack(), world, context.getBlockPos(), context.getSide(), false, true, true);
        }
        return IModCompatHandler.super.preventWandUseOnBlock(context);
    }
     */

    private Method mainApiClassGetMethod;
    private Method mainApiClassGetChunkApiMethod;
    private Method protectionMethod;

    private final boolean enabled;

    public OpenPACModCompatHandler() {
        boolean enabled;
        try {
            Class<?> mainApiClass = Class.forName("xaero.pac.common.server.api.OpenPACServerAPI");
            Class<?> chunkApiClass = Class.forName("xaero.pac.common.server.claims.protection.api.IChunkProtectionAPI");
            this.mainApiClassGetMethod = mainApiClass.getMethod("get", net.minecraft.server.MinecraftServer.class);
            this.mainApiClassGetChunkApiMethod = mainApiClass.getMethod("getChunkProtection");
            this.protectionMethod = chunkApiClass.getMethod("onBlockInteraction", Entity.class, InteractionHand.class, ItemStack.class, ServerLevel.class, BlockPos.class, Direction.class, boolean.class, boolean.class, boolean.class);
            enabled = true;
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            ConstructionWand.LOGGER.warn("Failed to load OpenPAC compat handler: {}", exception.getMessage());
            enabled = false;
        }
        this.enabled = enabled;
    }

    @Override
    public boolean preventWandUseOnBlock(UseOnContext context) {
        if (!this.enabled) return false;
        boolean result = false;
        if (context.getLevel() instanceof ServerLevel world) {
            try {
                Object mainApiInstance = this.mainApiClassGetMethod.invoke(null, world.getServer());
                Object chunkApiInstance = this.mainApiClassGetChunkApiMethod.invoke(mainApiInstance);
                result = (boolean) this.protectionMethod.invoke(chunkApiInstance, context.getPlayer(), context.getHand(), context.getItemInHand(), world, context.getClickedPos(), context.getClickedFace(), false, true, true);
            } catch (Throwable ignored) {
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean shouldCancelBlockPlacement(Level w, Player player, BlockState blockState, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {
        if (!this.enabled) return false;
        boolean result = false;

        InteractionHand hand;
        if (player.getMainHandItem().getItem() instanceof WandItem) hand = InteractionHand.MAIN_HAND;
        else hand = InteractionHand.OFF_HAND;

        if (w instanceof ServerLevel world) {
            try {
                Object mainApiInstance = this.mainApiClassGetMethod.invoke(null, world.getServer());
                Object chunkApiInstance = this.mainApiClassGetChunkApiMethod.invoke(mainApiInstance);
                result = (boolean) this.protectionMethod.invoke(chunkApiInstance, player, hand, player.getItemInHand(hand), world, pos, null, false, false, true);
            } catch (Throwable ignored) {
            }
            return result;
        }
        return false;
    }
}
