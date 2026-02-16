package dev.smto.constructionwand.integrations.mod;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IModCompatHandler;
import dev.smto.constructionwand.items.wand.WandItem;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

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
            this.protectionMethod = chunkApiClass.getMethod("onBlockInteraction", Entity.class, Hand.class, ItemStack.class, ServerWorld.class, BlockPos.class, Direction.class, boolean.class, boolean.class, boolean.class);
            enabled = true;
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            ConstructionWand.LOGGER.warn("Failed to load OpenPAC compat handler: {}", exception.getMessage());
            enabled = false;
        }
        this.enabled = enabled;
    }

    @Override
    public boolean preventWandUseOnBlock(ItemUsageContext context) {
        if (!this.enabled) return false;
        boolean result = false;
        if (context.getWorld() instanceof ServerWorld world) {
            try {
                Object mainApiInstance = mainApiClassGetMethod.invoke(null, world.getServer());
                Object chunkApiInstance = mainApiClassGetChunkApiMethod.invoke(mainApiInstance);
                result = (boolean) protectionMethod.invoke(chunkApiInstance, context.getPlayer(), context.getHand(), context.getStack(), world, context.getBlockPos(), context.getSide(), false, true, true);
            } catch (Throwable ignored) {}
            return result;
        }
        return false;
    }

    @Override
    public boolean shouldCancelBlockPlacement(World w, PlayerEntity player, BlockState blockState, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {
        if (!this.enabled) return false;
        boolean result = false;

        Hand hand;
        if (player.getMainHandStack().getItem() instanceof WandItem) hand = Hand.MAIN_HAND;
        else hand = Hand.OFF_HAND;

        if (w instanceof ServerWorld world) {
            try {
                Object mainApiInstance = mainApiClassGetMethod.invoke(null, world.getServer());
                Object chunkApiInstance = mainApiClassGetChunkApiMethod.invoke(mainApiInstance);
                result = (boolean) protectionMethod.invoke(chunkApiInstance, player, hand, player.getStackInHand(hand), world, pos, null, false, false, true);
            } catch (Throwable ignored) {}
            return result;
        }
        return false;
    }
}
