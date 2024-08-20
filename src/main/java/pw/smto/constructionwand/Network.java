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

    public static class Payloads {
        public record S2CHandshake(boolean value) implements CustomPayload {
            public static final CustomPayload.Id<S2CHandshake> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "s2c_handshake"));
            public static final PacketCodec<RegistryByteBuf, S2CHandshake> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, S2CHandshake::value, S2CHandshake::new);

            @Override
            public Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record C2SHandshakeCallback(boolean value) implements CustomPayload {
            public static final CustomPayload.Id<C2SHandshakeCallback> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "c2s_handshake_callback"));
            public static final PacketCodec<RegistryByteBuf, C2SHandshakeCallback> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, C2SHandshakeCallback::value, C2SHandshakeCallback::new);

            @Override
            public Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record C2SWandUpgradeRequest(boolean value) implements CustomPayload {
            public static final CustomPayload.Id<C2SWandUpgradeRequest> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "c2s_wand_upgrade_request"));
            public static final PacketCodec<RegistryByteBuf, C2SWandUpgradeRequest> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, C2SWandUpgradeRequest::value, C2SWandUpgradeRequest::new);

            @Override
            public Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

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
            public static final PacketCodec<RegistryByteBuf, C2SQueryUndoPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, C2SQueryUndoPayload::undoPressed, C2SQueryUndoPayload::new);

            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }
        }

        public record C2SWandOptionPayload(String key, String value, boolean notify1) implements CustomPayload {
            public static final CustomPayload.Id<C2SWandOptionPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "wand_option"));
            public static final PacketCodec<RegistryByteBuf, C2SWandOptionPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, C2SWandOptionPayload::key, PacketCodecs.STRING, C2SWandOptionPayload::value, PacketCodecs.BOOL, C2SWandOptionPayload::notify1, C2SWandOptionPayload::new);

            @Override
            public CustomPayload.Id<? extends CustomPayload> getId() {
                return ID;
            }

            public static C2SWandOptionPayload of(IOption<?> option, boolean notify) {
                return new C2SWandOptionPayload(option.getKey(), option.getValueString(), notify);
            }
        }
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(Payloads.S2CHandshake.ID, Payloads.S2CHandshake.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.S2CUndoBlocksPayload.ID, Payloads.S2CUndoBlocksPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(Payloads.C2SHandshakeCallback.ID, Payloads.C2SHandshakeCallback.CODEC);
        PayloadTypeRegistry.playC2S().register(Payloads.C2SWandUpgradeRequest.ID, Payloads.C2SWandUpgradeRequest.CODEC);
        PayloadTypeRegistry.playC2S().register(Payloads.C2SQueryUndoPayload.ID, Payloads.C2SQueryUndoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Payloads.C2SWandOptionPayload.ID, Payloads.C2SWandOptionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SHandshakeCallback.ID, (payload, context) -> {
            context.server().execute(() -> {
                ConstructionWand.LOGGER.info("Enabling client-side enhancements for player: " + context.player().getDisplayName().getString());
                PlayerInstance.getEntryFromPlayerEntity(context.player()).hasClientMod = true;
                context.player().getInventory().markDirty();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SWandUpgradeRequest.ID, (payload, context) -> {
            context.server().execute(() -> {
                ItemStack stack = context.player().getMainHandStack();
                if (!(stack.getItem() instanceof ItemWand)) return;
                WandUtil.upgradeWand(context.player(), stack, null);
                context.player().getInventory().markDirty();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SQueryUndoPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if(player == null) return;
                PlayerInstance.updateClient(player, payload.undoPressed);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SWandOptionPayload.ID, (payload, context) -> {
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
