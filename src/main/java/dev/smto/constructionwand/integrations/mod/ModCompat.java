package dev.smto.constructionwand.integrations.mod;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.api.IModCompatHandler;
import dev.smto.constructionwand.api.SnapshotCreationContext;
import dev.smto.constructionwand.containers.ContainerManager;
import dev.smto.constructionwand.integrations.polymer.PolymerManager;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

public class ModCompat {
    private static boolean checked = false;

    public static boolean polymerEnabled = false;

    private static final HashMap<String, Supplier<IModCompatHandler>> SUPPORTED_COMPAT = new HashMap<>() {{
        this.put("create", CreateModCompatHandler::new);
        this.put("openpartiesandclaims", OpenPACModCompatHandler::new);
        this.put("bankstorage", BankStorageModCompatHandler::new);
        this.put("packedup", PackedUpModCompatHandler::new);
        this.put("sophisticatedbackpacks", SophisticatedBackpackModCompatHandler::new);
        this.put("botania", BotaniaModCompatHandler::new);
    }};

    private static final HashSet<IModCompatHandler> ENABLED_COMPAT = new HashSet<>();

    public static void init() {
        if (checked) return;

        FabricLoader.getInstance().getAllMods().forEach(mod -> {
            if (SUPPORTED_COMPAT.containsKey(mod.getMetadata().getId())) {
                var inst = SUPPORTED_COMPAT.get(mod.getMetadata().getId()).get();
                ENABLED_COMPAT.add(inst);
                if (inst instanceof IContainerHandler) {
                    ContainerManager.register((IContainerHandler) inst);
                }
                ConstructionWand.LOGGER.info("Enabling mod compat for {}", mod.getMetadata().getName());
            }
        });

        polymerEnabled = FabricLoader.getInstance().isModLoaded(ConstructionWand.MOD_ID + "-polymer");
        if (polymerEnabled) PolymerManager.init();

        checked = true;
    }

    /**
     * Adds a mod compat handler for your mod.<br><br>
     * A handler allows you to add custom behavior to the wand when interacting with blocks added by your mod.<br>
     * Check out {@link CreateModCompatHandler} for an example implementation, but note that all builtin handlers use reflection instead of APIs to reduce maintenance and dependency issues.<br><br>
     * In addition, a class implementing IModCompatHandler can also implement IContainerHandler,
     * which will allow a wand to use the inventory of an item added by your mod.<br><br>
     * @param handler Your mod compat handler instance
     */
    public static void addModCompatHandler(IModCompatHandler handler) {
        ENABLED_COMPAT.add(handler);
        if (handler instanceof IContainerHandler) {
            ContainerManager.register((IContainerHandler) handler);
        }
    }

    // hook for WandItem.useOnBlock
    public static boolean preventWandOnBlock(ItemUsageContext context) {
        boolean out = false;
        for (IModCompatHandler iModCompat : ENABLED_COMPAT) {
            if (out) break;
            out = iModCompat.preventWandUseOnBlock(context);
        }
        return out;
    }

    // hook for PlaceSnapshot.get
    public static SnapshotCreationContext mutateSnapshot(SnapshotCreationContext context) {
        for (IModCompatHandler iModCompat : ENABLED_COMPAT) context = iModCompat.onSnapshotCreation(context);
        return context;
    }

    // hook for WandUtil.placeBlock
    public static void afterBlockPlacement(World world, PlayerEntity player, BlockState block, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {
        for (IModCompatHandler iModCompat : ENABLED_COMPAT) iModCompat.afterBlockPlacement(world, player, block, pos, item, includedItem);
    }

    // hook for WandUtil.placeBlock
    public static boolean shouldCancelBlockPlacement(World world, PlayerEntity player, BlockState block, BlockPos pos, @Nullable ItemStack item, @Nullable ItemStack includedItem) {
        boolean out = false;
        for (IModCompatHandler iModCompat : ENABLED_COMPAT) {
            if (out) break;
            out = iModCompat.shouldCancelBlockPlacement(world, player, block, pos, item, includedItem);
        }
        return out;
    }

    // hook for WandUtil.hasBlockEntity
    public static boolean allowBlockEntityRemoval(World world, BlockPos pos, BlockEntity blockEntity) {
        boolean out = false;
        for (IModCompatHandler iModCompat : ENABLED_COMPAT) {
            if (out) break;
            out = iModCompat.allowBlockEntityRemoval(world, pos, blockEntity);
        }
        return out;
    }
}
