package dev.smto.constructionwand.containers;

import dev.smto.constructionwand.api.IContainerHandler;
import dev.smto.constructionwand.containers.handlers.HandlerBundle;
import dev.smto.constructionwand.containers.handlers.HandlerNBTInventory;
import dev.smto.constructionwand.containers.handlers.HandlerShulkerbox;
import dev.smto.constructionwand.integrations.mod.ModCompat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashSet;

public class ContainerManager
{
    private static final HashSet<IContainerHandler> HANDLERS = new HashSet<IContainerHandler>();

    public static void register(IContainerHandler handler) {
        HANDLERS.add(handler);
    }

    public static int countItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack) {
        for(IContainerHandler handler : HANDLERS) {
            if(handler.matches(player, itemStack, inventoryStack)) {
                return handler.countItems(player, itemStack, inventoryStack);
            }
        }
        return 0;
    }

    public static int useItems(PlayerEntity player, ItemStack itemStack, ItemStack inventoryStack, int count) {
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

        ContainerManager.register(new HandlerShulkerbox());
        ContainerManager.register(new HandlerNBTInventory());
        ContainerManager.register(new HandlerBundle());

        initialized = true;
    }
}