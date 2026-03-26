package dev.smto.constructionwand;

import dev.smto.constructionwand.api.WandConfigEntry;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.IOption;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.items.wand.WandItem;
import dev.smto.constructionwand.wand.undo.UndoHistory;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;

public class Network {

    public static class Payloads {
        public record S2CUndoBlocksPayload(List<BlockPos> blockPosList) implements CustomPacketPayload {
            public static final CustomPacketPayload.Type<S2CUndoBlocksPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "undo_blocks"));
            public static final StreamCodec<ByteBuf, List<BlockPos>> PACKET_CODEC = new StreamCodec<ByteBuf, List<BlockPos>>() {
                public List<BlockPos> decode(ByteBuf byteBuf) {
                    if (byteBuf.readableBytes() <= 0) return new ArrayList<>();
                    List<BlockPos> out = new ArrayList<>();
                    while (byteBuf.readableBytes() > 0) {
                        out.add(FriendlyByteBuf.readBlockPos(byteBuf));
                    }
                    return out;
                }

                public void encode(ByteBuf byteBuf, List<BlockPos> blockPosList) {
                    if (blockPosList.isEmpty()) return;
                    for (BlockPos blockPos : blockPosList) {
                        FriendlyByteBuf.writeBlockPos(byteBuf, blockPos);
                    }
                }
            };
            public static final StreamCodec<RegistryFriendlyByteBuf, S2CUndoBlocksPayload> CODEC = StreamCodec.composite(PACKET_CODEC, S2CUndoBlocksPayload::blockPosList, S2CUndoBlocksPayload::new);

            @Override
            public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
                return ID;
            }
        }

        public record C2SQueryUndoPayload(boolean undoPressed) implements CustomPacketPayload {
            public static final CustomPacketPayload.Type<C2SQueryUndoPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "query_undo"));
            public static final StreamCodec<RegistryFriendlyByteBuf, C2SQueryUndoPayload> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, C2SQueryUndoPayload::undoPressed, C2SQueryUndoPayload::new);

            @Override
            public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
                return ID;
            }
        }

        public record C2SWandOptionPayload(String key, String value, boolean shouldNotify) implements CustomPacketPayload {
            public static final CustomPacketPayload.Type<C2SWandOptionPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "wand_option"));
            public static final StreamCodec<RegistryFriendlyByteBuf, C2SWandOptionPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, C2SWandOptionPayload::key, ByteBufCodecs.STRING_UTF8, C2SWandOptionPayload::value, ByteBufCodecs.BOOL, C2SWandOptionPayload::shouldNotify, C2SWandOptionPayload::new);

            @Override
            public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
                return ID;
            }

            public static C2SWandOptionPayload of(IOption<?> option, boolean notify) {
                return new C2SWandOptionPayload(option.getKey(), option.getValueString(), notify);
            }
        }

        public record S2CPing(boolean unused) implements CustomPacketPayload {
            public static final CustomPacketPayload.Type<S2CPing> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "pong"));
            public static final StreamCodec<RegistryFriendlyByteBuf, S2CPing> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, S2CPing::unused, S2CPing::new);
            @Override
            public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
                return ID;
            }
        }

        public record C2SPong(boolean unused) implements CustomPacketPayload {
            public static final CustomPacketPayload.Type<C2SPong> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "pong"));
            public static final StreamCodec<RegistryFriendlyByteBuf, C2SPong> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, C2SPong::unused, C2SPong::new);
            @Override
            public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
                return ID;
            }
        }

        public record S2CSyncModConfigPayload(List<Integer> ints, List<Boolean> booleans, List<String> similarBlocks, List<String> blockEntityList, List<WandConfigEntry> wands) implements CustomPacketPayload {
            public static final CustomPacketPayload.Type<S2CSyncModConfigPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "mod_config"));
            public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncModConfigPayload> CODEC = StreamCodec.composite(
                    ByteBufCodecs.INT.apply(ByteBufCodecs.list()), S2CSyncModConfigPayload::ints,
                    ByteBufCodecs.BOOL.apply(ByteBufCodecs.list()), S2CSyncModConfigPayload::booleans,
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), S2CSyncModConfigPayload::similarBlocks,
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), S2CSyncModConfigPayload::blockEntityList,
                    WandConfigEntry.PACKET_CODEC.apply(ByteBufCodecs.list()), S2CSyncModConfigPayload::wands,
                    S2CSyncModConfigPayload::new
            );

            public static S2CSyncModConfigPayload create() {
                return new S2CSyncModConfigPayload(
                        List.of(ConstructionWand.Config.maxRange, ConstructionWand.Config.maxInfinityCreativeRange, ConstructionWand.Config.undoHistorySize),
                        List.of(ConstructionWand.Config.angelFalling, ConstructionWand.Config.whitelist),
                        ConstructionWand.Config.similarBlocks,
                        ConstructionWand.Config.blockEntityList,
                        List.of(ConstructionWand.Config.stoneWand, ConstructionWand.Config.ironWand, ConstructionWand.Config.diamondWand, ConstructionWand.Config.infinityWand)
                );
            }

            public static void apply(S2CSyncModConfigPayload payload) {
                if (payload.ints().size() != 3) {
                    ConstructionWand.LOGGER.warn("Received invalid S2CSyncModConfigPayload from server!");
                    return;
                }
                if (payload.booleans().size() != 2) {
                    ConstructionWand.LOGGER.warn("Received invalid S2CSyncModConfigPayload from server!");
                    return;
                }
                if (payload.wands().size() != 4) {
                    ConstructionWand.LOGGER.warn("Received invalid S2CSyncModConfigPayload from server!");
                    return;
                }

                ConstructionWand.configManager = null; // remove config manager so data is not saved to disk

                ConstructionWand.Config.maxRange = payload.ints().getFirst();
                ConstructionWand.Config.maxInfinityCreativeRange = payload.ints().get(1);
                ConstructionWand.Config.undoHistorySize = payload.ints().get(2);
                ConstructionWand.Config.angelFalling = payload.booleans().getFirst();
                ConstructionWand.Config.whitelist = payload.booleans().getLast();
                ConstructionWand.Config.similarBlocks = new ArrayList<>(payload.similarBlocks());
                ConstructionWand.Config.blockEntityList = new ArrayList<>(payload.blockEntityList());
                ConstructionWand.Config.stoneWand = payload.wands().getFirst();
                ConstructionWand.Config.ironWand = payload.wands().get(1);
                ConstructionWand.Config.diamondWand = payload.wands().get(2);
                ConstructionWand.Config.infinityWand = payload.wands().getLast();
            }

            @Override
            public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
                return ID;
            }
        }
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(Payloads.S2CUndoBlocksPayload.ID, Payloads.S2CUndoBlocksPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.S2CSyncModConfigPayload.ID, Payloads.S2CSyncModConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.S2CPing.ID, Payloads.S2CPing.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(Payloads.C2SQueryUndoPayload.ID, Payloads.C2SQueryUndoPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Payloads.C2SWandOptionPayload.ID, Payloads.C2SWandOptionPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Payloads.C2SPong.ID, Payloads.C2SPong.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SQueryUndoPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if(player == null) return;
                UndoHistory.updateClient(player, payload.undoPressed);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SWandOptionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if(player == null) return;

                ItemStack wand = WandUtil.holdingWand(player);
                if(wand == null) return;
                WandOptions options = WandOptions.of(wand);

                IOption<?> option = options.get(payload.key);
                if(option == null) return;
                option.setValueString(payload.value);

                if(payload.shouldNotify) WandItem.optionMessage(player, option);
                options.writeToStack();
                player.getInventory().setChanged();
            });
        });

        ServerPlayConnectionEvents.JOIN.register((ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) -> {
            if (server.isDedicatedServer()) ServerPlayNetworking.send(handler.player, Payloads.S2CSyncModConfigPayload.create());
            else ConstructionWand.ensureConfigManager();
        });
    }
}
