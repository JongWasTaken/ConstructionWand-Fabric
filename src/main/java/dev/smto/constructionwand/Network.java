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
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static dev.smto.constructionwand.ConstructionWand.MOD_ID;

public class Network {

    public static class Payloads {
        public record S2CUndoBlocksPayload(List<BlockPos> blockPosList) implements CustomPayload {
            public static final CustomPayload.Id<S2CUndoBlocksPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "undo_blocks"));
            public static final PacketCodec<ByteBuf, List<BlockPos>> PACKET_CODEC = new PacketCodec<ByteBuf, List<BlockPos>>() {
                public List<BlockPos> decode(ByteBuf byteBuf) {
                    if (byteBuf.readableBytes() <= 0) return new ArrayList<>();
                    List<BlockPos> out = new ArrayList<>();
                    while (byteBuf.readableBytes() > 0) {
                        out.add(PacketByteBuf.readBlockPos(byteBuf));
                    }
                    return out;
                }

                public void encode(ByteBuf byteBuf, List<BlockPos> blockPosList) {
                    if (blockPosList.isEmpty()) return;
                    for (BlockPos blockPos : blockPosList) {
                        PacketByteBuf.writeBlockPos(byteBuf, blockPos);
                    }
                }
            };
            public static final PacketCodec<RegistryByteBuf, S2CUndoBlocksPayload> CODEC = PacketCodec.tuple(PACKET_CODEC, S2CUndoBlocksPayload::blockPosList, S2CUndoBlocksPayload::new);

            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record C2SQueryUndoPayload(boolean undoPressed) implements CustomPayload {
            public static final CustomPayload.Id<C2SQueryUndoPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "query_undo"));
            public static final PacketCodec<RegistryByteBuf, C2SQueryUndoPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, C2SQueryUndoPayload::undoPressed, C2SQueryUndoPayload::new);

            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record C2SWandOptionPayload(String key, String value, boolean shouldNotify) implements CustomPayload {
            public static final CustomPayload.Id<C2SWandOptionPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "wand_option"));
            public static final PacketCodec<RegistryByteBuf, C2SWandOptionPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, C2SWandOptionPayload::key, PacketCodecs.STRING, C2SWandOptionPayload::value, PacketCodecs.BOOLEAN, C2SWandOptionPayload::shouldNotify, C2SWandOptionPayload::new);

            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }

            public static C2SWandOptionPayload of(IOption<?> option, boolean notify) {
                return new C2SWandOptionPayload(option.getKey(), option.getValueString(), notify);
            }
        }

        public record S2CPing(boolean unused) implements CustomPayload {
            public static final CustomPayload.Id<S2CPing> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "pong"));
            public static final PacketCodec<RegistryByteBuf, S2CPing> CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, S2CPing::unused, S2CPing::new);
            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record C2SPong(boolean unused) implements CustomPayload {
            public static final CustomPayload.Id<C2SPong> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "pong"));
            public static final PacketCodec<RegistryByteBuf, C2SPong> CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, C2SPong::unused, C2SPong::new);
            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record S2CSyncModConfigPayload(List<Integer> ints, List<Boolean> booleans, List<String> similarBlocks, List<String> blockEntityList, List<WandConfigEntry> wands) implements CustomPayload {
            public static final CustomPayload.Id<S2CSyncModConfigPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "mod_config"));
            public static final PacketCodec<RegistryByteBuf, S2CSyncModConfigPayload> CODEC = PacketCodec.tuple(
                    PacketCodecs.INTEGER.collect(PacketCodecs.toList()), S2CSyncModConfigPayload::ints,
                    PacketCodecs.BOOLEAN.collect(PacketCodecs.toList()), S2CSyncModConfigPayload::booleans,
                    PacketCodecs.STRING.collect(PacketCodecs.toList()), S2CSyncModConfigPayload::similarBlocks,
                    PacketCodecs.STRING.collect(PacketCodecs.toList()), S2CSyncModConfigPayload::blockEntityList,
                    WandConfigEntry.PACKET_CODEC.collect(PacketCodecs.toList()), S2CSyncModConfigPayload::wands,
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
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }
        }
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(Payloads.S2CUndoBlocksPayload.ID, Payloads.S2CUndoBlocksPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.S2CSyncModConfigPayload.ID, Payloads.S2CSyncModConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.S2CPing.ID, Payloads.S2CPing.CODEC);

        PayloadTypeRegistry.playC2S().register(Payloads.C2SQueryUndoPayload.ID, Payloads.C2SQueryUndoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Payloads.C2SWandOptionPayload.ID, Payloads.C2SWandOptionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Payloads.C2SPong.ID, Payloads.C2SPong.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SQueryUndoPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if(player == null) return;
                UndoHistory.updateClient(player, payload.undoPressed);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SWandOptionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if(player == null) return;

                ItemStack wand = WandUtil.holdingWand(player);
                if(wand == null) return;
                WandOptions options = WandOptions.of(wand);

                IOption<?> option = options.get(payload.key);
                if(option == null) return;
                option.setValueString(payload.value);

                if(payload.shouldNotify) WandItem.optionMessage(player, option);
                options.writeToStack();
                player.getInventory().markDirty();
            });
        });

        ServerPlayConnectionEvents.JOIN.register((ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) -> {
            if (server.isDedicated()) ServerPlayNetworking.send(handler.player, Payloads.S2CSyncModConfigPayload.create());
            else ConstructionWand.ensureConfigManager();
        });
    }
}
