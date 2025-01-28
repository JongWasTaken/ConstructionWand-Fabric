package pw.smto.constructionwand;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.impl.core.ForgeConfigSpecAdapter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.neoforged.fml.config.ModConfig;
import pw.smto.constructionwand.basics.ConfigClient;
import pw.smto.constructionwand.client.ClientEvents;
import pw.smto.constructionwand.client.Network;

@Environment(EnvType.CLIENT)
public class ConstructionWandClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NeoForgeConfigRegistry.INSTANCE.register(ConstructionWand.MOD_ID, ModConfig.Type.CLIENT, new ForgeConfigSpecAdapter(ConfigClient.SPEC));
        Network.init();
        ClientEvents.init();
    }
}
