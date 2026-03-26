package dev.smto.constructionwand;

import com.mojang.blaze3d.platform.InputConstants;
import dev.smto.constructionwand.client.ClientEvents;
import dev.smto.constructionwand.client.Network;
import dev.smto.simpleconfig.ConfigLoggers;
import dev.smto.simpleconfig.SimpleConfig;
import dev.smto.simpleconfig.api.ConfigAnnotations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public class ConstructionWandClient implements ClientModInitializer {
    public static final SimpleConfig CONFIG_MANAGER = new SimpleConfig(
            FabricLoader.getInstance().getConfigDir().resolve("construction_wand_client.conf"),
            ConstructionWandClient.Config.class,
            ConfigLoggers.create(ConstructionWand.LOGGER::debug, ConstructionWand.LOGGER::info, ConstructionWand.LOGGER::warn, ConstructionWand.LOGGER::error)
    );

    public static KeyMapping optKey;
    public static KeyMapping optionalMenuKey;

    @Override
    public void onInitializeClient() {
        Network.init();
        var cat = KeyMapping.Category.register(ConstructionWand.id("all"));

        optKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.constructionwand.opt_key", InputConstants.Type.KEYSYM, 341, cat));
        optionalMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.constructionwand.menu_key", InputConstants.Type.KEYSYM, 86, cat));

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
