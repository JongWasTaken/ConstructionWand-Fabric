package pw.smto.constructionwand.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import pw.smto.constructionwand.api.IContainerHandler;
import pw.smto.constructionwand.containers.handlers.HandlerBundleComponent;
import pw.smto.constructionwand.containers.handlers.HandlerContainerComponent;
import pw.smto.constructionwand.containers.handlers.HandlerNBTInventory;
import pw.smto.constructionwand.integrations.ModCompat;

import java.util.ArrayList;

public class ContainerManager
{
    private static final ArrayList<IContainerHandler> handlers = new ArrayList<IContainerHandler>();

    public static boolean register(IContainerHandler handler) {
        if (handlers.contains(handler)) return false;
        return handlers.add(handler);
    }

    public static int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        for(IContainerHandler handler : handlers) {
            if(handler.matches(player, itemStack, inventoryStack)) {
                return handler.countItems(player, itemStack, inventoryStack);
            }
        }
        return 0;
    }

    public static int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        for(IContainerHandler handler : handlers) {
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

        ModCompat.registerModContainerHandlers();

        initialized = true;
    }
}