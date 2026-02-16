package dev.smto.constructionwand.integrations.mod;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IModCompatHandler;
import dev.smto.constructionwand.api.SnapshotCreationContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CreateModCompatHandler implements IModCompatHandler {
    // Adding the actual mod as a compile-time dependency caused a name conflict, so I just decided to make compat handlers use reflection instead
    private Class<?> copycatBlockClass;
    private Class<?> copycatBlockEntityClass;

    private final boolean enabled;

    public CreateModCompatHandler() {
        // because we use reflection instead of an api, we have to check if the classes we need are present at runtime and disable the compat handler if they aren't
        boolean enabled;
        try {
            // this is specifically for create-fly, as other versions of create don't exist for recent fabric minecraft versions
            this.copycatBlockClass = Class.forName("com.zurrtum.create.content.decoration.copycat.CopycatBlock");
            this.copycatBlockEntityClass = Class.forName("com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity");
            enabled = true;
        } catch (ClassNotFoundException exception) {
            ConstructionWand.LOGGER.warn("Failed to load Create compat handler: {}", exception.getMessage());
            enabled = false;
        }
        this.enabled = enabled;
    }

    @Override
    public SnapshotCreationContext onSnapshotCreation(SnapshotCreationContext context) {
        // pulls the included item from the block entity of the block being copied, if it's a copycat block
        if (!this.enabled) return context;
        if (!context.getWorld().isClient()) {
            if (this.copycatBlockClass.isAssignableFrom(context.getSupportingBlockState().getBlock().getClass())) {
                var gbe = context.getWorld().getBlockEntity(context.getRayTraceResult().getBlockPos());
                if (gbe == null) return context;
                if (gbe.getClass().isAssignableFrom(this.copycatBlockEntityClass)) {
                    try {
                        var includedItem = ((ItemStack) this.copycatBlockEntityClass.getMethod("getConsumedItem").invoke(gbe));
                        context.setGiveBackIncludedItem(false);
                        if (includedItem.getItem() == Items.AIR) includedItem = null;
                        context.setIncludedItemStack(includedItem);
                    } catch (Throwable ignored) {}
                }
            }
        }
        return context;
    }

    @Override
    public void afterBlockPlacement(World world, PlayerEntity player, BlockState blockState, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {
        // sets the block entity data for a placed copycat block to match the block that was copied, if applicable
        if (!this.enabled) return;
        if (includedItem != null) {
            if (this.copycatBlockClass.isAssignableFrom(blockState.getBlock().getClass())) {
                var gbe = world.getBlockEntity(pos);
                if (gbe == null) return;
                if (gbe.getClass().isAssignableFrom(this.copycatBlockEntityClass)) {
                    try {
                        this.copycatBlockEntityClass.getMethod("setMaterial", BlockState.class).invoke(gbe,((BlockItem)includedItem.getItem()).getBlock().getDefaultState());
                        this.copycatBlockEntityClass.getMethod("setConsumedItem", ItemStack.class).invoke(gbe, includedItem);
                    } catch (Throwable ignored) {}
                }
            }
        }
    }

    @Override
    public boolean allowBlockEntityRemoval(World world, BlockPos pos, BlockEntity blockEntity) {
        // makes copycat blocks removable by the wand
        if (!this.enabled) return false;
        return blockEntity.getClass().isAssignableFrom(this.copycatBlockEntityClass);
    }
}
