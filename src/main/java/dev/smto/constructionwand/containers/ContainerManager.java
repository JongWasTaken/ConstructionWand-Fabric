package dev.smto.constructionwand.containers;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.containers.handlers.HandlerBundleComponent;
import dev.smto.constructionwand.containers.handlers.HandlerContainerComponent;
import dev.smto.constructionwand.containers.handlers.HandlerNBTInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;

public class ContainerManager {
    private static final HashSet<IContainerHandler> HANDLERS = new HashSet<>();

    public static void register(IContainerHandler handler) {
        ContainerManager.HANDLERS.add(handler);
    }

    public static int countItems(Player player, ItemStack itemStack, ItemStack inventoryStack) {
        for (IContainerHandler handler : ContainerManager.HANDLERS) {
            if (handler.matches(player, itemStack, inventoryStack)) {
                return handler.countItems(player, itemStack, inventoryStack);
            }
        }
        return 0;
    }

    public static int useItems(Player player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        for (IContainerHandler handler : ContainerManager.HANDLERS) {
            if (handler.matches(player, itemStack, inventoryStack)) {
                return handler.useItems(player, itemStack, inventoryStack, count);
            }
        }
        return count;
    }

    private static boolean initialized;

    public static void init() {
        if (ContainerManager.initialized) return;

        ContainerManager.register(new HandlerContainerComponent());
        ContainerManager.register(new HandlerNBTInventory());
        ContainerManager.register(new HandlerBundleComponent());

        ContainerManager.initialized = true;
    }
}