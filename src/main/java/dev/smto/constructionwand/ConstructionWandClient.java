package dev.smto.constructionwand;

import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.client.ClientEvents;
import dev.smto.constructionwand.client.Network;
import dev.smto.constructionwand.items.wand.WandItem;
import dev.smto.simpleconfig.ConfigLoggers;
import dev.smto.simpleconfig.SimpleConfig;
import dev.smto.simpleconfig.api.ConfigAnnotations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;

@Environment(EnvType.CLIENT)
public class ConstructionWandClient implements ClientModInitializer {
    public static final SimpleConfig CONFIG_MANAGER = new SimpleConfig(
            FabricLoader.getInstance().getConfigDir().resolve("construction_wand_client.conf"),
            ConstructionWandClient.Config.class,
            ConfigLoggers.create(ConstructionWand.LOGGER::debug, ConstructionWand.LOGGER::info, ConstructionWand.LOGGER::warn, ConstructionWand.LOGGER::error)
    );

    public static KeyBinding optKey;
    public static KeyBinding optionalMenuKey;

    @Override
    public void onInitializeClient() {
        for(Item item : ConstructionWand.getRegistry().getWands()) {
            ModelPredicateProviderRegistry.register(
                    item, ConstructionWand.id("using_core"),
                    (stack, world, entity, seed) -> {
                        if (entity == null || !(stack.getItem() instanceof WandItem)) return 0;
                        return WandOptions.of(stack).cores.get().getColor() > -1 ? 1 : 0;
                    }
            );
            ColorProviderRegistry.ITEM.register((stack, layer) -> {
                if (layer == 1 && stack.getItem() instanceof WandItem) {
                    return WandOptions.of(stack).cores.get().getColor();
                }
                return -1;
            }, item);
        }
        Network.init();
        optKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.constructionwand.opt_key", InputUtil.Type.KEYSYM, 341, "key.category.constructionwand"));
        optionalMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.constructionwand.menu_key", InputUtil.Type.KEYSYM, 86, "key.category.constructionwand"));

        ClientEvents.init();
    }

    public static class Config {
        @ConfigAnnotations.Header(header = "ConstructionWand client config")
        @ConfigAnnotations.Comment(comment = "Press SNEAK+OPTKEY instead of SNEAK for opening wand GUI")
        public static boolean requireOptKeyForMenu = false;
        @ConfigAnnotations.Comment(comment = "Press SNEAK+OPTKEY instead of SNEAK for changing wand mode/direction lock")
        public static boolean requireOptKeyForActions = false;
    }
}
