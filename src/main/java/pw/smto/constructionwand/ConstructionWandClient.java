package pw.smto.constructionwand;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.impl.core.ForgeConfigSpecAdapter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.neoforged.fml.config.ModConfig;
import pw.smto.constructionwand.basics.ConfigClient;
import pw.smto.constructionwand.client.ClientEvents;
import pw.smto.constructionwand.client.Network;

@Environment(EnvType.CLIENT)
public class ConstructionWandClient implements ClientModInitializer {
    public static KeyBinding optKey;

    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(ConstructionWand.MOD_ID, ModConfig.Type.CLIENT, new ForgeConfigSpecAdapter(ConfigClient.SPEC));
        Network.init();
        optKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.constructionwand.opt_key", InputUtil.Type.KEYSYM, ConfigClient.OPT_KEY.get(), "key.category.constructionwand"));
        ClientEvents.init();
    }
}
