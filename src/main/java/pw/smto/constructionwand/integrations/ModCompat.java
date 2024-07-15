package pw.smto.constructionwand.integrations;

import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    public static boolean CREATE = false;

    public static void init() {
        CREATE = FabricLoader.getInstance().isModLoaded("create");
    }
}
