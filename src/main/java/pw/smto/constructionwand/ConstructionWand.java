package pw.smto.constructionwand;

import dev.smto.simpleconfig.ConfigLoggers;
import dev.smto.simpleconfig.SimpleConfig;
import dev.smto.simpleconfig.api.ConfigAnnotations;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pw.smto.constructionwand.api.WandConfigEntry;
import pw.smto.constructionwand.basics.ReplacementRegistry;
import pw.smto.constructionwand.containers.ContainerManager;
import pw.smto.constructionwand.integrations.ModCompat;
import pw.smto.constructionwand.wand.undo.UndoHistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstructionWand implements ModInitializer
{
    public static final String MOD_ID = "constructionwand";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final SimpleConfig CONFIG_MANAGER = new SimpleConfig(
            FabricLoader.getInstance().getConfigDir().resolve("construction_wand.conf"),
            Config.class,
            ConfigLoggers.create(LOGGER::debug, LOGGER::info, LOGGER::warn, LOGGER::error),
            Map.of("stoneWand", WandConfigEntry.CODEC, "ironWand", WandConfigEntry.CODEC, "diamondWand", WandConfigEntry.CODEC, "infinityWand", WandConfigEntry.CODEC, "similarBlocks", Identifier.CODEC.listOf())
    );

    public static final Map<Item, WandConfigEntry> WAND_CONFIG_MAP = new HashMap<>();

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("ConstructionWand says hello - may the odds be ever in your favor.");
        Registry.registerAll();
        WAND_CONFIG_MAP.put(Registry.Items.STONE_WAND, Config.stoneWand);
        WAND_CONFIG_MAP.put(Registry.Items.IRON_WAND, Config.ironWand);
        WAND_CONFIG_MAP.put(Registry.Items.DIAMOND_WAND, Config.diamondWand);
        WAND_CONFIG_MAP.put(Registry.Items.INFINITY_WAND, Config.infinityWand);
        Network.init();
        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            ReplacementRegistry.init();
            ModCompat.checkForMods();
            ContainerManager.init();
        });
        ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> UndoHistory.removePlayerEntity(handler.player));
    }

    public static class Config {
        @ConfigAnnotations.Section(section = "General settings")
        @ConfigAnnotations.Comment(comment = "Maximum placement range (0: unlimited). Affects all wands and is meant for lag prevention, not game balancing.")
        public static int maxRange = 100;
        @ConfigAnnotations.Comment(comment = "Block limit for Infinity Wand used in creative mode.")
        public static int maxInfinityCreativeRange = 2048;
        @ConfigAnnotations.Comment(comment = "Number of operations that can be undone.")
        public static int undoHistorySize = 3;
        @ConfigAnnotations.Comment(comment = "Blocks to treat equally when in Similar mode.")
        public static ArrayList<Identifier> similarBlocks = new ArrayList<>(List.of(
                Identifier.ofVanilla("dirt"),
                Identifier.ofVanilla("grass_block"),
                Identifier.ofVanilla("coarse_dirt"),
                Identifier.ofVanilla("podzol"),
                Identifier.ofVanilla("mycelium"),
                Identifier.ofVanilla("farmland"),
                Identifier.ofVanilla("dirt_path"),
                Identifier.ofVanilla("rooted_dirt")
        ));
        @ConfigAnnotations.Comment(comment = "Place blocks below you while falling > 10 blocks with angel core (Can be used to save you from drops/the void)")
        public static boolean angelFalling = false;

        @ConfigAnnotations.Section(section = "Block Entity settings")
        @ConfigAnnotations.Comment(comment = "If set to true, treat blockEntityList as a whitelist, otherwise blacklist")
        public static boolean whitelist = false;

        @ConfigAnnotations.Comment(comment = "Allow/Prevent blocks with TEs from being placed by wand.\nYou can either add block ids like \"minecraft:chest\" or mod ids like \"minecraft\".")
        public static ArrayList<String> blockEntityList = new ArrayList<>(List.of(
                "chiselsandbits"
        ));

        @ConfigAnnotations.Section(section = "Wand-specific settings")
        @ConfigAnnotations.Comment(comment = "Settings for the stone wand")
        public static WandConfigEntry stoneWand = new WandConfigEntry(false, 131, 9, 0, 0);

        @ConfigAnnotations.Comment(comment = "Settings for the iron wand")
        public static WandConfigEntry ironWand = new WandConfigEntry(true, 250, 27, 2, 9);

        @ConfigAnnotations.Comment(comment = "Settings for the diamond wand")
        public static WandConfigEntry diamondWand = new WandConfigEntry(true, 1561, 128, 8, 25);

        @ConfigAnnotations.Comment(comment = "Settings for the infinity wand")
        public static WandConfigEntry infinityWand = new WandConfigEntry(true, -1, 1024, 16, 81);


    }
}
