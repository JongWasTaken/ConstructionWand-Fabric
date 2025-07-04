package dev.smto.constructionwand.basics;
//import com.copycatsplus.copycats.content.copycat.shaft.CopycatShaftBlock;
//import com.simibubi.create.content.decoration.copycat.CopycatBlock;
//import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.containers.ContainerManager;
import dev.smto.constructionwand.integrations.ModCompat;
import dev.smto.constructionwand.items.wand.WandItem;
import dev.smto.constructionwand.wand.WandItemUseContext;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class WandUtil
{
    public static boolean stackEquals(ItemStack stackA, ItemStack stackB) {
        if (stackIsInvalid(stackA)) return false;
        if (stackIsInvalid(stackB)) return false;
        return ItemStack.areItemsEqual(stackA, stackB);
    }

    private static boolean stackIsInvalid(ItemStack stack) {
        if (!stack.getComponentChanges().equals(ComponentChanges.EMPTY)) {
            return true;
        }
        // fail if stack in question contains items (shulker box destruction prevention tm)
        if (stack.contains(DataComponentTypes.CONTAINER)) {
            if (!Objects.equals(stack.get(DataComponentTypes.CONTAINER), ContainerComponent.DEFAULT)) return true;
        }

        return false;
    }

    public static boolean stackEquals(ItemStack stackA, Item item) {
        ItemStack stackB = new ItemStack(item);
        return stackEquals(stackA, stackB);
    }


    public static ItemStack convertPolymerStack(ItemStack stack) {
        if (stack.getComponents().contains(DataComponentTypes.CUSTOM_DATA)) {
            var nbt = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).copyNbt();
            if (nbt.contains("$polymer:stack")) {
                nbt = nbt.getCompound("$polymer:stack").orElse(new NbtCompound());
                if (nbt.contains("id")) {
                    Identifier id = Identifier.tryParse(nbt.getString("id").orElse(""));
                    if (id != null) {
                        Item item = Registries.ITEM.get(id);
                        if (item != null) {
                            ItemStack newStack = item.getDefaultStack();
                            try {
                                nbt = nbt.getCompound("components").orElse(new NbtCompound()).getCompound("minecraft:custom_data").orElse(new NbtCompound());
                            } catch (Exception ignored) {}
                            newStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                            return newStack;
                        }
                    }
                }
            }
        }
        return stack;
    }

    public static ItemStack holdingWand(PlayerEntity player) {
        if(player.getStackInHand(Hand.MAIN_HAND) != ItemStack.EMPTY) {
            ItemStack stack = convertPolymerStack(player.getStackInHand(Hand.MAIN_HAND));
            if (stack.getItem() instanceof WandItem) return stack;
        }
        if(player.getStackInHand(Hand.OFF_HAND) != ItemStack.EMPTY) {
            ItemStack stack = convertPolymerStack(player.getStackInHand(Hand.OFF_HAND));
            if (stack.getItem() instanceof WandItem) return stack;
        }
        return null;
    }

    public static BlockPos posFromVec(Vec3d vec) {
        return new BlockPos(
                (int) Math.round(vec.x), (int) Math.round(vec.y), (int) Math.round(vec.z));
    }

    public static Vec3d entityPositionVec(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ());
    }

    public static Vec3d blockPosVec(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static List<ItemStack> getHotbar(PlayerEntity player) {
        return player.getInventory().getMainStacks().subList(0, 9);
    }

    public static List<ItemStack> getHotbarWithOffhand(PlayerEntity player) {
        ArrayList<ItemStack> inventory = new ArrayList<>(player.getInventory().getMainStacks().subList(0, 9));
        inventory.add(player.getOffHandStack());
        return inventory;
    }

    public static List<ItemStack> getMainInv(PlayerEntity player) {
        return player.getInventory().getMainStacks().subList(9, player.getInventory().getMainStacks().size());
    }

    public static List<ItemStack> getFullInv(PlayerEntity player) {
        ArrayList<ItemStack> inventory = new ArrayList<>();
        inventory.add(player.getOffHandStack());
        inventory.addAll(player.getInventory().getMainStacks());
        return inventory;
    }

    public static int blockDistance(BlockPos p1, BlockPos p2) {
        return Math.max(Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getZ() - p2.getZ()));
    }

    public static boolean isTEAllowed(BlockState state) {
        if(!state.hasBlockEntity()) return true;

        Identifier name = Registries.BLOCK.getId(state.getBlock());
        if(name == null) return false;

        String fullId = name.toString();
        String modId = name.getNamespace();

        boolean inList = ConstructionWand.Config.blockEntityList.contains(fullId) || ConstructionWand.Config.blockEntityList.contains(modId);
        boolean isWhitelist = ConstructionWand.Config.whitelist;

        return isWhitelist == inList;
    }

    public static boolean placeBlock(World world, PlayerEntity player, BlockState block, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {
        if(!world.setBlockState(pos, block)) {
            ConstructionWand.LOGGER.info("Block could not be placed");
            return false;
        }

        ItemStack stack;
        if(item == null) stack = new ItemStack(block.getBlock().asItem());
        else {
            stack = item;
            player.increaseStat(Stats.USED.getOrCreateStat(item.getItem()), 1);
        }

        // Call OnBlockPlaced method
        block.getBlock().onPlaced(world, pos, block, player, stack);

        if (includedItem != null) {
            // Create Copycats compat
            if (ModCompat.create) {
                //if (block.getBlock() instanceof CopycatBlock c) {
                //    var cEnt = c.getBlockEntity(world, pos);
                //    if (cEnt != null) {
                //        cEnt.setMaterial(((BlockItem)includedItem.getItem()).getBlock().getDefaultState());
                //        cEnt.setConsumedItem(includedItem);
                //    }
                //}
            }
        }

        return true;
    }

    public static boolean removeBlock(World world, PlayerEntity player, @Nullable BlockState block, BlockPos pos) {
        BlockState currentBlock = world.getBlockState(pos);

        if(!world.canEntityModifyAt(player, pos)) return false;

        if(!player.isCreative()) {
            boolean hasEntity = false;

            var ent = world.getBlockEntity(pos);

            if (ent != null) {
                hasEntity = true;
                if (ModCompat.create) {
                    //if (ent instanceof CopycatBlockEntity) {
                    //    hasEntity = false;
                    //}
                }
            }

            if(currentBlock.getHardness(world, pos) <= -1 || hasEntity) return false;

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

    public static int countItem(PlayerEntity player, Item item) {
        if(player.getInventory().getMainStacks() == null) return 0;
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

    private static boolean isPositionModifiable(World world, PlayerEntity player, BlockPos pos) {
        // Is position out of world?
        if(!world.isInBuildLimit(pos)) return false;

        // Is block modifiable?
        if(!world.canEntityModifyAt(player, pos)) return false;

        // Limit range
        if(ConstructionWand.Config.maxRange > 0 &&
                WandUtil.blockDistance(player.getBlockPos(), pos) > ConstructionWand.Config.maxRange) return false;

        return true;
    }

    /**
     * Tests if a wand can place a block at a certain position.
     * This check is independent of the used block.
     */
    public static boolean isPositionPlaceable(World world, PlayerEntity player, BlockPos pos, boolean replace) {
        if(!isPositionModifiable(world, player, pos)) return false;

        // If replace mode is off, target has to be air
        if(world.isAir(pos)) return true;

        // Otherwise, check if the block can be replaced by a generic block
        return replace && world.getBlockState(pos).canReplace(
                new WandItemUseContext(world, player,
                        new BlockHitResult(new Vec3d(0, 0, 0), Direction.DOWN, pos, false),
                        pos, (BlockItem) Items.STONE));
    }

    public static boolean isBlockRemovable(World world, PlayerEntity player, BlockPos pos) {
        if(!isPositionModifiable(world, player, pos)) return false;

        if(!player.isCreative()) {
            return !(world.getBlockState(pos).getHardness(world, pos) <= -1) && world.getBlockEntity(pos) == null;
        }
        return true;
    }

    public static boolean isBlockPermeable(World world, BlockPos pos) {
        return world.isAir(pos) || world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
    }

    public static boolean entitiesCollidingWithBlock(World world, BlockState blockState, BlockPos pos) {
        VoxelShape shape = blockState.getCollisionShape(world, pos);
        if(!shape.isEmpty()) {
            Box blockBB = shape.getBoundingBox().offset(pos);
            return !world.getEntitiesByClass(LivingEntity.class, blockBB, Predicate.not(Entity::isSpectator)).isEmpty();
        }
        return false;
    }

    public static Direction fromVector(Vec3d vector) {
        return Direction.getFacing(vector.x, vector.y, vector.z);
    }
}
