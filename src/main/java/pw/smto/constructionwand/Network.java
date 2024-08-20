package pw.smto.constructionwand;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import pw.smto.constructionwand.wand.undo.UndoHistory;

import java.util.ArrayList;
import java.util.List;

import static pw.smto.constructionwand.ConstructionWand.MOD_ID;

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
        PayloadTypeRegistry.playS2C().register(Payloads.S2CUndoBlocksPayload.ID, Payloads.S2CUndoBlocksPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(Payloads.C2SQueryUndoPayload.ID, Payloads.C2SQueryUndoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Payloads.C2SWandOptionPayload.ID, Payloads.C2SWandOptionPayload.CODEC);


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
}
