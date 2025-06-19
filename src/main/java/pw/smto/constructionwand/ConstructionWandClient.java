package pw.smto.constructionwand;

import dev.smto.simpleconfig.ConfigLoggers;
import dev.smto.simpleconfig.SimpleConfig;
import dev.smto.simpleconfig.api.ConfigAnnotations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import pw.smto.constructionwand.client.ClientEvents;
import pw.smto.constructionwand.client.Network;

@Environment(EnvType.CLIENT)
public class ConstructionWandClient implements ClientModInitializer {
    public static final SimpleConfig CONFIG_MANAGER = new SimpleConfig(
            FabricLoader.getInstance().getConfigDir().resolve("construction_wand_client.conf"),
            ConstructionWandClient.Config.class,
            ConfigLoggers.create(ConstructionWand.LOGGER::debug, ConstructionWand.LOGGER::info, ConstructionWand.LOGGER::warn, ConstructionWand.LOGGER::error)
    );

    public static KeyBinding optKey;

    @Override
    public void onInitializeClient() {
        Network.init();
        optKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.constructionwand.opt_key", InputUtil.Type.KEYSYM, 341, "key.category.constructionwand"));
        ClientEvents.init();
    }

    public static class Config {
        @ConfigAnnotations.Section(section = "Client config")
        @ConfigAnnotations.Comment(comment = "Press SNEAK+OPTKEY instead of SNEAK for opening wand GUI")
        public static boolean requireOptKeyForMenu = false;
        @ConfigAnnotations.Comment(comment = "Press SNEAK+OPTKEY instead of SNEAK for changing wand mode/direction lock")
        public static boolean requireOptKeyForActions = false;
    }
}
