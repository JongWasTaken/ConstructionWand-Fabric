package dev.smto.constructionwand;

import com.mojang.serialization.Codec;
import dev.smto.constructionwand.api.ModRegistry;
import dev.smto.constructionwand.api.WandConfigEntry;
import dev.smto.constructionwand.basics.ReplacementRegistry;
import dev.smto.constructionwand.containers.ContainerManager;
import dev.smto.constructionwand.integrations.ModCompat;
import dev.smto.constructionwand.wand.undo.UndoHistory;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstructionWand implements ModInitializer
{
    public static final String MOD_ID = "constructionwand";
    public static final Logger LOGGER = LogManager.getLogger();
    private static ModRegistry REGISTRY;

    public static SimpleConfig configManager = null;

    public static void ensureConfigManager() {
        if (configManager != null) return;
        ConstructionWand.configManager = new SimpleConfig(
                FabricLoader.getInstance().getConfigDir().resolve("construction_wand.conf"),
                Config.class,
                ConfigLoggers.create(LOGGER::debug, LOGGER::info, LOGGER::warn, LOGGER::error),
                Map.of("stoneWand", WandConfigEntry.CODEC, "ironWand", WandConfigEntry.CODEC, "diamondWand", WandConfigEntry.CODEC, "infinityWand", WandConfigEntry.CODEC, "similarBlocks", Codec.STRING.listOf())
        );
    }

    public static final Map<Item, Field> WAND_CONFIG_MAP = new HashMap<>();

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    public static ModRegistry getRegistry() {
        return REGISTRY;
    }

    @Override
    public void onInitialize() {
        ensureConfigManager();
        Network.init();
        ModCompat.checkForMods();
        REGISTRY = new DefaultRegistry();
        LOGGER.info("ConstructionWand says hello - may the odds be ever in your favor.");
        REGISTRY.registerAll();
        try {
            WAND_CONFIG_MAP.put(REGISTRY.getStoneWand(), Config.class.getField("stoneWand"));
            WAND_CONFIG_MAP.put(REGISTRY.getIronWand(), Config.class.getField("ironWand"));
            WAND_CONFIG_MAP.put(REGISTRY.getDiamondWand(), Config.class.getField("diamondWand"));
            WAND_CONFIG_MAP.put(REGISTRY.getInfinityWand(), Config.class.getField("infinityWand"));
        } catch (Throwable ignored) {}
        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            ReplacementRegistry.init();
            ContainerManager.init();
        });
        ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> UndoHistory.removePlayerEntity(handler.player));
    }

    public static class Config {
        @ConfigAnnotations.Header(header = "ConstructionWand common config")
        @ConfigAnnotations.Section(section = "General settings")
        @ConfigAnnotations.Comment(comment = "Maximum placement range (0: unlimited). Affects all wands and is meant for lag prevention, not game balancing.")
        public static int maxRange = 100;
        @ConfigAnnotations.Comment(comment = "Block limit for Infinity Wand used in creative mode.")
        public static int maxInfinityCreativeRange = 2048;
        @ConfigAnnotations.Comment(comment = "Number of operations that can be undone.")
        public static int undoHistorySize = 3;
        @ConfigAnnotations.Comment(comment = "Blocks to treat equally when in Similar mode. Separate with ;")
        public static ArrayList<String> similarBlocks = new ArrayList<>(List.of("minecraft:dirt;minecraft:grass_block;minecraft:coarse_dirt;minecraft:podzol;minecraft:mycelium;minecraft:farmland;minecraft:dirt_path;minecraft:rooted_dirt"));

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

        @ConfigAnnotations.Comment(comment = "Settings for the infinity wand (durability ignored)")
        public static WandConfigEntry infinityWand = new WandConfigEntry(true, -1, 1024, 16, 81);


    }
}
