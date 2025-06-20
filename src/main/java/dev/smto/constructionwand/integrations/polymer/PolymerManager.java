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
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PolymerManager {
    private static final HashSet<UUID> PLAYERS_WITH_CLIENT = new HashSet<>();
    private static final HashSet<UUID> PLAYERS_WITH_BLOCKED_SCREENS = new HashSet<>();

    public static void init() {
        PolymerResourcePackUtils.addModAssets(ConstructionWand.MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        ServerPlayConnectionEvents.JOIN.register((ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) -> {
            ServerPlayNetworking.send(handler.player, Network.Payloads.S2CPing.getId(), Network.Payloads.S2CPing.encode(new Network.Payloads.S2CPing(false)));
        });

        ServerPlayNetworking.registerGlobalReceiver(Network.Payloads.C2SPong.ID, (MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {
            server.execute(() -> {
                if(player == null) return;
                ConstructionWand.LOGGER.info("Player {} connected with polymer client support!", player.getName().getString());
                PLAYERS_WITH_CLIENT.add(player.getUuid());
            });
        });
    }

    public static boolean hasClientMod(PlayerEntity player) {
        return PLAYERS_WITH_CLIENT.contains(player.getUuid());
    }

    public static void blockServerScreen(PlayerEntity player) {
        PLAYERS_WITH_BLOCKED_SCREENS.add(player.getUuid());
    }

    public static void unblockServerScreen(PlayerEntity player) {
        PLAYERS_WITH_BLOCKED_SCREENS.remove(player.getUuid());
    }

    public static boolean isScreenBlocked(PlayerEntity player) {
        return PLAYERS_WITH_BLOCKED_SCREENS.contains(player.getUuid());
    }

    public static void openServerScreen(ServerPlayerEntity player, ItemStack wand) {
        WandServerScreen.open(player, wand);
    }

    private static class WandServerScreen extends SimpleGui {
        private final ItemStack wand;
        private final WandOptions options;
        private final ServerPlayerEntity player;

        private static ItemStack getIcon(IOption<?> option) {
            var key = option.get();
            if (key instanceof IWandCore core) {
                if (core instanceof PolymerDestructionCoreItem) {
                    return ConstructionWand.getRegistry().getDestructionCore().getDefaultStack();
                }
                if (core instanceof PolymerAngelCoreItem) {
                    return ConstructionWand.getRegistry().getAngelCore().getDefaultStack();

                }
                return Items.BRICKS.getDefaultStack();
            }
            if (key instanceof WandOptions.Lock lock) {
                switch (lock) {
                    case HORIZONTAL:
                        return Items.OAK_SLAB.getDefaultStack();
                    case VERTICAL:
                        return Items.OAK_FENCE.getDefaultStack();
                    case NORTHSOUTH:
                        return Items.RAIL.getDefaultStack();
                    case EASTWEST:
                        return Items.POWERED_RAIL.getDefaultStack();
                }
            }
            if (key instanceof WandOptions.Direction direction) {
                return switch (direction) {
                    case TARGET -> Items.OAK_LOG.getDefaultStack();
                    case PLAYER -> Items.PLAYER_HEAD.getDefaultStack();
                };
            }
            if (key instanceof WandOptions.Match match) {
                switch (match) {
                    case EXACT:
                        return Items.GRASS_BLOCK.getDefaultStack();
                    case SIMILAR:
                        return Items.DIRT.getDefaultStack();
                    case ANY:
                        return Items.PUMPKIN.getDefaultStack();
                }
            }
            if (key instanceof WandOptions.Match match) {
                switch (match) {
                    case EXACT:
                        return Items.GRASS_BLOCK.getDefaultStack();
                    case SIMILAR:
                        return Items.DIRT.getDefaultStack();
                    case ANY:
                        return Items.PUMPKIN.getDefaultStack();
                }
            }
            if (key instanceof Boolean b) {
                if (option.getKey().equals("replace")) {
                    if (b) return Items.WATER_BUCKET.getDefaultStack();
                    return Items.BARRIER.getDefaultStack();
                }
                if (option.getKey().equals("random")) {
                    if (b) return Items.COMMAND_BLOCK.getDefaultStack();
                    return Items.BEDROCK.getDefaultStack();
                }
            }

            return Items.STICK.getDefaultStack();
        }

        private WandServerScreen(ServerPlayerEntity player, ItemStack wand) {
            super(ScreenHandlerType.GENERIC_9X1, player, false);
            this.player = player;
            this.wand = wand;
            this.options = WandOptions.of(wand);
            this.setTitle(wand.getName());
        }

        @Override
        public void beforeOpen() {
            super.beforeOpen();
            for (int i = 0; i < options.allOptions.length; i++) {
                this.addSlot(buildElement(options.allOptions[i]));
            }
            this.setSlot(8, GuiElementBuilder.from(Items.EXPERIENCE_BOTTLE.getDefaultStack())
                    .setName(
                            ((MutableText)PolymerStat.getName(ConstructionWand.getRegistry().getUseWandStat())).formatted(Formatting.AQUA)
                    )
                    .addLoreLine(Text.literal(String.valueOf(this.player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ConstructionWand.getRegistry().getUseWandStat())))).formatted(Formatting.GRAY))
                    .setCallback((int index, ClickType type, SlotActionType action) -> {
                    })
                    .build()
            );
        }

        private Text getCapitalizedKey(IOption<?> option) {
            var str = Text.translatable(option.getKey()).getString();
            str = str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
            str = switch (str) {
                case "Cores" -> "Selected Core";
                case "Lock" -> "Restriction";
                case "Replace" -> "Replacement";
                case "Match" -> "Matching";
                default -> str;
            };
            return Text.literal(str).formatted(Formatting.AQUA);
        }

        private GuiElement buildElement(IOption<?> option) {
            return GuiElementBuilder.from(getIcon(option))
                    .setLore(List.of())
                    .setName(getCapitalizedKey(option))
                    .addLoreLine(Text.translatable(option.getValueTranslation()).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(" "))
                    .addLoreLine(Text.translatable(option.getDescTranslation()).formatted(Formatting.GRAY))
                    .setCallback((int index, ClickType type, SlotActionType action) -> {
                        option.next(!type.isRight);
                        this.setSlot(index, buildElement(option));
                    })
                    .build();
        }

        @Override
        public void onClose() {
            super.onClose();
            this.options.writeToStack(this.wand);
            player.getInventory().markDirty();
        }

        public static void open(ServerPlayerEntity player, ItemStack wand) {
            new WandServerScreen(player, wand).open();
        }
    }
}
