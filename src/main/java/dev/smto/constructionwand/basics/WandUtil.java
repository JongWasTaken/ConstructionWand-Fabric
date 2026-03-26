package dev.smto.constructionwand.basics;
//import com.copycatsplus.copycats.content.copycat.shaft.CopycatShaftBlock;
//import com.simibubi.create.content.decoration.copycat.CopycatBlock;
//import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.containers.ContainerManager;
import dev.smto.constructionwand.integrations.mod.ModCompat;
import dev.smto.constructionwand.items.wand.WandItem;
import dev.smto.constructionwand.wand.WandItemUseContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WandUtil
{
    public static boolean stackEquals(ItemStack stackA, ItemStack stackB) {
        if (stackIsInvalid(stackA)) return false;
        if (stackIsInvalid(stackB)) return false;
        return ItemStack.isSameItem(stackA, stackB);
    }

    private static boolean stackIsInvalid(ItemStack stack) {
        if (!stack.getComponentsPatch().equals(DataComponentPatch.EMPTY)) {
            return true;
        }
        // fail if stack in question contains items (shulker box destruction prevention tm)
        if (stack.has(DataComponents.CONTAINER)) {
            if (!Objects.equals(stack.get(DataComponents.CONTAINER), ItemContainerContents.EMPTY)) return true;
        }

        return false;
    }

    public static boolean stackEquals(ItemStack stackA, Item item) {
        ItemStack stackB = new ItemStack(item);
        return stackEquals(stackA, stackB);
    }


    public static ItemStack convertPolymerStack(ItemStack stack) {
        if (stack.getComponents().has(DataComponents.CUSTOM_DATA)) {
            var nbt = Objects.requireNonNull(stack.get(DataComponents.CUSTOM_DATA)).copyTag();
            if (nbt.contains("$polymer:stack")) {
                nbt = nbt.getCompound("$polymer:stack").orElse(new CompoundTag());
                if (nbt.contains("id")) {
                    Identifier id = Identifier.tryParse(nbt.getString("id").orElse(""));
                    if (id != null) {
                        Item item = BuiltInRegistries.ITEM.getValue(id);
                        if (item != null) {
                            ItemStack newStack = item.getDefaultInstance();
                            try {
                                nbt = nbt.getCompound("components").orElse(new CompoundTag()).getCompound("minecraft:custom_data").orElse(new CompoundTag());
                            } catch (Exception ignored) {}
                            newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                            return newStack;
                        }
                    }
                }
            }
        }
        return stack;
    }

    public static ItemStack holdingWand(Player player) {
        if(player.getItemInHand(InteractionHand.MAIN_HAND) != ItemStack.EMPTY) {
            ItemStack stack = convertPolymerStack(player.getItemInHand(InteractionHand.MAIN_HAND));
            if (stack.getItem() instanceof WandItem) return stack;
        }
        if(player.getItemInHand(InteractionHand.OFF_HAND) != ItemStack.EMPTY) {
            ItemStack stack = convertPolymerStack(player.getItemInHand(InteractionHand.OFF_HAND));
            if (stack.getItem() instanceof WandItem) return stack;
        }
        return null;
    }

    public static BlockPos posFromVec(Vec3 vec) {
        return new BlockPos(
                (int) Math.round(vec.x), (int) Math.round(vec.y), (int) Math.round(vec.z));
    }

    public static Vec3 entityPositionVec(Entity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ());
    }

    public static Vec3 blockPosVec(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static List<ItemStack> getHotbar(Player player) {
        return player.getInventory().getNonEquipmentItems().subList(0, 9);
    }

    public static List<ItemStack> getHotbarWithOffhand(Player player) {
        ArrayList<ItemStack> inventory = new ArrayList<>(player.getInventory().getNonEquipmentItems().subList(0, 9));
        inventory.add(player.getOffhandItem());
        return inventory;
    }

    public static List<ItemStack> getMainInv(Player player) {
        return player.getInventory().getNonEquipmentItems().subList(9, player.getInventory().getNonEquipmentItems().size());
    }

    public static List<ItemStack> getFullInv(Player player) {
        ArrayList<ItemStack> inventory = new ArrayList<>();
        inventory.add(player.getOffhandItem());
        inventory.addAll(player.getInventory().getNonEquipmentItems());
        return inventory;
    }

    public static int blockDistance(BlockPos p1, BlockPos p2) {
        return Math.max(Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getZ() - p2.getZ()));
    }

    public static boolean isTEAllowed(BlockState state) {
        if(!state.hasBlockEntity()) return true;

        Identifier name = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if(name == null) return false;

        String fullId = name.toString();
        String modId = name.getNamespace();

        boolean inList = ConstructionWand.Config.blockEntityList.contains(fullId) || ConstructionWand.Config.blockEntityList.contains(modId);
        boolean isWhitelist = ConstructionWand.Config.whitelist;

        return isWhitelist == inList;
    }

    public static boolean placeBlock(Level world, Player player, BlockState block, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {
        if (ModCompat.shouldCancelBlockPlacement(world, player, block, pos, item, includedItem)) {
            return false;
        }

        if(!world.setBlockAndUpdate(pos, block)) {
            ConstructionWand.LOGGER.info("Block could not be placed");
            return false;
        }

        ItemStack stack;
        if(item == null) stack = new ItemStack(block.getBlock().asItem());
        else {
            stack = item;
            player.awardStat(Stats.ITEM_USED.get(item.getItem()), 1);
        }

        // Call OnBlockPlaced method
        block.getBlock().setPlacedBy(world, pos, block, player, stack);

        // Let mods do their thing
        ModCompat.afterBlockPlacement(world, player, block, pos, item, includedItem);

        return true;
    }

    public static boolean removeBlock(Level world, Player player, @Nullable BlockState block, BlockPos pos) {
        BlockState currentBlock = world.getBlockState(pos);

        if(!world.mayInteract(player, pos)) return false;

        if(!player.isCreative()) {
            boolean hasEntity = hasBlockEntity(world, pos);

            if(currentBlock.getDestroySpeed(world, pos) <= -1 || hasEntity) return false;

            if(block != null)
                if(!ReplacementRegistry.matchBlocks(currentBlock.getBlock(), block.getBlock())) return false;
        }

        /*
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, currentBlock, player);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        if(breakEvent.isCanceled()) return false;
         */

        world.removeBlock(pos, false);
        return true;
    }

    public static int countItem(Player player, Item item) {
        if(player.getInventory().getNonEquipmentItems() == null) return 0;
        if(player.isCreative()) return Integer.MAX_VALUE;

        int total = 0;
        List<ItemStack> inventory = WandUtil.getFullInv(player);

        for(ItemStack stack : inventory) {
            if(stack == null || stack.isEmpty()) continue;

            if(WandUtil.stackEquals(stack, item)) {
                total += stack.getCount();
            }
            else {
                int amount = ContainerManager.countItems(player, new ItemStack(item), stack);
                if(amount == Integer.MAX_VALUE) return Integer.MAX_VALUE;
                total += amount;
            }
        }
        return total;
    }

    private static boolean isPositionModifiable(Level world, Player player, BlockPos pos) {
        // Is position out of world?
        if(!world.isInWorldBounds(pos)) return false;

        // Is block modifiable?
        if(!world.mayInteract(player, pos)) return false;

        // Limit range
        if(ConstructionWand.Config.maxRange > 0 &&
                WandUtil.blockDistance(player.blockPosition(), pos) > ConstructionWand.Config.maxRange) return false;

        return true;
    }

    /**
     * Tests if a wand can place a block at a certain position.
     * This check is independent of the used block.
     */
    public static boolean isPositionPlaceable(Level world, Player player, BlockPos pos, boolean replace) {
        if(!isPositionModifiable(world, player, pos)) return false;

        // If replace mode is off, target has to be air
        if(world.isEmptyBlock(pos)) return true;

        // Otherwise, check if the block can be replaced by a generic block
        return replace && world.getBlockState(pos).canBeReplaced(
                new WandItemUseContext(world, player,
                        new BlockHitResult(new Vec3(0, 0, 0), Direction.DOWN, pos, false),
                        pos, (BlockItem) Items.STONE));
    }

    public static boolean isBlockRemovable(Level world, Player player, BlockPos pos) {
        if(!isPositionModifiable(world, player, pos)) return false;

        if(!player.isCreative()) {
            return !(world.getBlockState(pos).getDestroySpeed(world, pos) <= -1) && !hasBlockEntity(world, pos);
        }
        return true;
    }

    public static boolean hasBlockEntity(Level world, BlockPos pos) {
        var ent = world.getBlockEntity(pos);
        boolean out = false;

        if (ent != null) {
            out = !ModCompat.allowBlockEntityRemoval(world, pos, ent);
        }
        return out;
    }

    public static boolean isBlockPermeable(Level world, BlockPos pos) {
        return world.isEmptyBlock(pos) || world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
    }

    public static boolean entitiesCollidingWithBlock(Level world, BlockState blockState, BlockPos pos) {
        VoxelShape shape = blockState.getCollisionShape(world, pos);
        if(!shape.isEmpty()) {
            AABB blockBB = shape.bounds().move(pos);
            return !world.getEntitiesOfClass(LivingEntity.class, blockBB, Predicate.not(Entity::isSpectator)).isEmpty();
        }
        return false;
    }

    public static Direction fromVector(Vec3 vector) {
        return Direction.getApproximateNearest(vector.x, vector.y, vector.z);
    }
}
