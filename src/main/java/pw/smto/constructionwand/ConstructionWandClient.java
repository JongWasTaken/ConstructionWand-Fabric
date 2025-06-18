package pw.smto.constructionwand;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
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
    public static KeyBinding optKey;

    @Override
    public void onInitializeClient() {
        NeoForgeConfigRegistry.INSTANCE.register(ConstructionWand.MOD_ID, ModConfig.Type.CLIENT, new ForgeConfigSpecAdapter(ConfigClient.SPEC));
        for(Item item : WANDS) {
            ModelPredicateProviderRegistry.register(
                    item, ConstructionWand.id("using_core"),
                    (stack, world, entity, seed) -> {
                        if (entity == null || !(stack.getItem() instanceof ItemWand)) return 0;
                        return new WandOptions(stack).cores.get().getColor() != -1 ? 1 : 0;
                    }
            );
            ColorProviderRegistry.ITEM.register((ItemStack stack, int layer) -> {
                // No clue what changed from 1.20.1 to 1.21, but colors work different now.
                // I used this site for conversion: https://argb-int-calculator.netlify.app/
                if (layer == 1 && stack.getItem() instanceof ItemWand) {
                    return new WandOptions(stack).cores.get().getColor();
                } else return -1;
            }, item);
        }
        Network.init();
        optKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.constructionwand.opt_key", InputUtil.Type.KEYSYM, ConfigClient.OPT_KEY.get(), "key.category.constructionwand"));
        ClientEvents.init();
    }
}
