package pw.smto.constructionwand;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.IOption;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.items.wand.ItemWand;
import pw.smto.constructionwand.wand.undo.PlayerInstance;

import java.util.ArrayList;
import java.util.List;

import static pw.smto.constructionwand.ConstructionWand.MOD_ID;

public class Network {

    public static class Channels {
        public record S2CHandshake(boolean value) implements CustomPayload {
            public static final CustomPayload.Id<S2CHandshake> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "s2c_handshake"));
            public static final PacketCodec<RegistryByteBuf, S2CHandshake> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, S2CHandshake::value, S2CHandshake::new);

            @Override
            public Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record S2CHandshakeCallback(boolean value) implements CustomPayload {
            public static final CustomPayload.Id<S2CHandshakeCallback> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "s2c_handshake_callback"));
            public static final PacketCodec<RegistryByteBuf, S2CHandshakeCallback> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, S2CHandshakeCallback::value, S2CHandshakeCallback::new);

            @Override
            public Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record UndoBlocksPayload(List<BlockPos> blockPosList) implements CustomPayload {
            public static final CustomPayload.Id<UndoBlocksPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "undo_blocks"));
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
            public static final PacketCodec<RegistryByteBuf, UndoBlocksPayload> CODEC = PacketCodec.tuple(PACKET_CODEC, UndoBlocksPayload::blockPosList, UndoBlocksPayload::new);

            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record QueryUndoPayload(boolean undoPressed) implements CustomPayload {
            public static final CustomPayload.Id<QueryUndoPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "query_undo"));
            public static final PacketCodec<RegistryByteBuf, QueryUndoPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, QueryUndoPayload::undoPressed, QueryUndoPayload::new);

            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record WandOptionPayload(String key, String value, boolean notify1) implements CustomPayload {
            public static final CustomPayload.Id<WandOptionPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "wand_option"));
            public static final PacketCodec<RegistryByteBuf, WandOptionPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, WandOptionPayload::key, PacketCodecs.STRING, WandOptionPayload::value, PacketCodecs.BOOL, WandOptionPayload::notify1, WandOptionPayload::new);

            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }

            public static WandOptionPayload of(IOption<?> option, boolean notify) {
                return new WandOptionPayload(option.getKey(), option.getValueString(), notify);
            }
        }
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(Channels.S2CHandshake.ID, Channels.S2CHandshake.CODEC);
        PayloadTypeRegistry.playC2S().register(Channels.S2CHandshakeCallback.ID, Channels.S2CHandshakeCallback.CODEC);
        PayloadTypeRegistry.playS2C().register(Channels.UndoBlocksPayload.ID, Channels.UndoBlocksPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Channels.QueryUndoPayload.ID, Channels.QueryUndoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Channels.WandOptionPayload.ID, Channels.WandOptionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Channels.S2CHandshakeCallback.ID, (payload, context) -> {
            context.server().execute(() -> {
                ConstructionWand.LOGGER.warn("Enabling packets for player: " + context.player().getDisplayName().getString());
                PlayerInstance.getEntryFromPlayerEntity(context.player()).hasClientMod = true;
                context.player().getInventory().markDirty();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Channels.QueryUndoPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if(player == null) return;
                PlayerInstance.updateClient(player, payload.undoPressed);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Channels.WandOptionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if(player == null) return;

                ItemStack wand = WandUtil.holdingWand(player);
                if(wand == null) return;
                WandOptions options = new WandOptions(wand);

                IOption<?> option = options.get(payload.key);
                if(option == null) return;
                option.setValueString(payload.value);

                if(payload.notify1) ItemWand.optionMessage(player, option);
                options.writeToStack(wand);
                player.getInventory().markDirty();
            });
        });
    }

    public static void sendPacket(PlayerEntity target, CustomPayload payload) {
        if (PlayerInstance.getEntryFromPlayerEntity(target).hasClientMod) {
            ServerPlayNetworking.send((ServerPlayerEntity) target, payload);
        }
    }
}
