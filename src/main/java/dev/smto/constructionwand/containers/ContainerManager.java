package dev.smto.constructionwand.containers;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.containers.handlers.HandlerBundleComponent;
import dev.smto.constructionwand.containers.handlers.HandlerContainerComponent;
import dev.smto.constructionwand.containers.handlers.HandlerNBTInventory;
import java.util.HashSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerManager
{
    private static final HashSet<IContainerHandler> HANDLERS = new HashSet<IContainerHandler>();

    public static void register(IContainerHandler handler) {
        HANDLERS.add(handler);
    }

    public static int countItems(Player player, ItemStack itemStack, ItemStack inventoryStack) {
        for(IContainerHandler handler : HANDLERS) {
            if(handler.matches(player, itemStack, inventoryStack)) {
                return handler.countItems(player, itemStack, inventoryStack);
            }
        }
        return 0;
    }

    public static int useItems(Player player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        for(IContainerHandler handler : HANDLERS) {
            if(handler.matches(player, itemStack, inventoryStack)) {
                return handler.useItems(player, itemStack, inventoryStack, count);
            }
        }
        return count;
    }

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        ContainerManager.register(new HandlerContainerComponent());
        ContainerManager.register(new HandlerNBTInventory());
        ContainerManager.register(new HandlerBundleComponent());

        initialized = true;
    }
}