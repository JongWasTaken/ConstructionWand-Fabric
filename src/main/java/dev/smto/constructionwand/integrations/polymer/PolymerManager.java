package dev.smto.constructionwand.integrations.polymer;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.Network;
import dev.smto.constructionwand.api.IWandCore;
import dev.smto.constructionwand.basics.option.IOption;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.integrations.polymer.item.PolymerAngelCoreItem;
import dev.smto.constructionwand.integrations.polymer.item.PolymerDestructionCoreItem;
import eu.pb4.polymer.core.api.other.PolymerStat;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PolymerManager {
    private static final HashSet<UUID> PLAYERS_WITH_CLIENT = new HashSet<>();
    private static final HashSet<UUID> PLAYERS_WITH_BLOCKED_SCREENS = new HashSet<>();

    public static void init() {
        ConstructionWand.LOGGER.info("Polymer addon detected, enabling Polymer compat!");
        PolymerResourcePackUtils.addModAssets(ConstructionWand.MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        ServerPlayConnectionEvents.JOIN.register((ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) -> {
            PLAYERS_WITH_CLIENT.remove(handler.player.getUUID());
            unblockServerScreen(handler.player);
            ServerPlayNetworking.send(handler.player, new Network.Payloads.S2CPing(false));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((ServerGamePacketListenerImpl handler, MinecraftServer minecraftServer) -> {
            PLAYERS_WITH_CLIENT.remove(handler.player.getUUID());
            unblockServerScreen(handler.player);
        });

        ServerPlayNetworking.registerGlobalReceiver(Network.Payloads.C2SPong.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                ConstructionWand.LOGGER.info("Player {} connected with polymer client support!", player.getName().tryCollapseToString());
                PLAYERS_WITH_CLIENT.add(player.getUUID());
            });
        });
    }

    public static boolean hasClientMod(UUID player) {
        return PLAYERS_WITH_CLIENT.contains(player);
    }

    public static void blockServerScreen(Player player) {
        PLAYERS_WITH_BLOCKED_SCREENS.add(player.getUUID());
    }

    public static void unblockServerScreen(Player player) {
        PLAYERS_WITH_BLOCKED_SCREENS.remove(player.getUUID());
    }

    public static boolean isScreenBlocked(Player player) {
        return PLAYERS_WITH_BLOCKED_SCREENS.contains(player.getUUID());
    }

    public static void openServerScreen(ServerPlayer player, ItemStack wand) {
        WandServerScreen.open(player, wand);
    }

    private static class WandServerScreen extends SimpleGui {
        private final ItemStack wand;
        private final WandOptions options;
        private final ServerPlayer player;

        private static ItemStack getIcon(IOption<?> option) {
            var key = option.get();
            if (key instanceof IWandCore core) {
                if (core instanceof PolymerDestructionCoreItem) {
                    var model = ConstructionWand.getRegistry().getDestructionCore().getDefaultInstance().get(DataComponents.ITEM_MODEL);
                    var item = Items.STICK.getDefaultInstance();
                    item.set(DataComponents.ITEM_MODEL, model);
                    return item;
                }
                if (core instanceof PolymerAngelCoreItem) {
                    var model = ConstructionWand.getRegistry().getAngelCore().getDefaultInstance().get(DataComponents.ITEM_MODEL);
                    var item = Items.STICK.getDefaultInstance();
                    item.set(DataComponents.ITEM_MODEL, model);
                    return item;
                }
                return Items.BRICKS.getDefaultInstance();
            }
            if (key instanceof WandOptions.Lock lock) {
                switch (lock) {
                    case HORIZONTAL:
                        return Items.OAK_SLAB.getDefaultInstance();
                    case VERTICAL:
                        return Items.OAK_FENCE.getDefaultInstance();
                    case NORTHSOUTH:
                        return Items.RAIL.getDefaultInstance();
                    case EASTWEST:
                        return Items.POWERED_RAIL.getDefaultInstance();
                }
            }
            if (key instanceof WandOptions.Direction direction) {
                return switch (direction) {
                    case TARGET -> Items.OAK_LOG.getDefaultInstance();
                    case PLAYER -> Items.PLAYER_HEAD.getDefaultInstance();
                };
            }
            if (key instanceof WandOptions.Match match) {
                return switch (match) {
                    case EXACT -> Items.GRASS_BLOCK.getDefaultInstance();
                    case SIMILAR -> Items.DIRT.getDefaultInstance();
                    case ANY -> Items.PUMPKIN.getDefaultInstance();
                };
            }
            if (key instanceof Boolean b) {
                if (option.getKey().equals("replace")) {
                    if (b) return Items.WATER_BUCKET.getDefaultInstance();
                    return Items.BARRIER.getDefaultInstance();
                }
                if (option.getKey().equals("random")) {
                    if (b) return Items.COMMAND_BLOCK.getDefaultInstance();
                    return Items.BEDROCK.getDefaultInstance();
                }
            }

            return Items.STICK.getDefaultInstance();
        }

        private WandServerScreen(ServerPlayer player, ItemStack wand) {
            super(MenuType.GENERIC_9x1, player, false);
            this.player = player;
            this.wand = wand;
            this.options = WandOptions.of(wand);
            this.setTitle(wand.getHoverName());
        }

        @Override
        public void beforeOpen() {
            super.beforeOpen();
            for (int i = 0; i < options.allOptions.length; i++) {
                this.addSlot(buildElement(options.allOptions[i]));
            }
            this.setSlot(8, GuiElementBuilder.from(Items.EXPERIENCE_BOTTLE.getDefaultInstance())
                    .hideDefaultTooltip()
                    .setName(
                            ((MutableComponent)PolymerStat.getName(ConstructionWand.getRegistry().getUseWandStat())).withStyle(ChatFormatting.AQUA)
                    )
                    .addLoreLine(Component.literal(String.valueOf(this.player.getStats().getValue(Stats.CUSTOM.get(ConstructionWand.getRegistry().getUseWandStat())))).withStyle(ChatFormatting.GRAY))
                    .setCallback((ClickType action) -> {})
                    .build()
            );
        }

        private Component getCapitalizedKey(IOption<?> option) {
            var str = Component.translatable(option.getKey()).getString();
            str = str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
            str = switch (str) {
                case "Cores" -> "Selected Core";
                case "Lock" -> "Restriction";
                case "Replace" -> "Replacement";
                case "Match" -> "Matching";
                default -> str;
            };
            return Component.literal(str).withStyle(ChatFormatting.AQUA);
        }

        private GuiElement buildElement(IOption<?> option) {
            return GuiElementBuilder.from(getIcon(option))
                    .setLore(List.of())
                    .hideDefaultTooltip()
                    .setName(getCapitalizedKey(option))
                    .addLoreLine(Component.translatable(option.getValueTranslation()).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(" "))
                    .addLoreLine(Component.translatable(option.getDescTranslation()).withStyle(ChatFormatting.GRAY))
                    .setCallback((int index, ClickType type, ContainerInput action, SlotBasedGui gui) -> {
                        option.next(!type.isRight);
                        this.setSlot(index, buildElement(option));
                    })
                    .build();
        }

        @Override
        public void onPlayerClose(boolean success) {
            // not sure if this is needed
            this.onManualClose();
            super.onPlayerClose(success);
        }

        @Override
        public void onManualClose() {
            super.onManualClose();
            this.options.writeToStack(this.wand);
            player.getInventory().setChanged();
        }

        public static void open(ServerPlayer player, ItemStack wand) {
            new WandServerScreen(player, wand).open();
        }
    }
}
