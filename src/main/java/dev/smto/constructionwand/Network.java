package dev.smto.constructionwand;

import dev.smto.constructionwand.api.WandConfigEntry;
import dev.smto.constructionwand.basics.WandUtil;
import dev.smto.constructionwand.basics.option.IOption;
import dev.smto.constructionwand.basics.option.WandOptions;
import dev.smto.constructionwand.items.wand.WandItem;
import dev.smto.constructionwand.wand.undo.UndoHistory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static dev.smto.constructionwand.ConstructionWand.id;

public class Network {

    public static class Payloads {
        public record S2CUndoBlocksPayload(List<BlockPos> blockPosList) {
            public static final Identifier ID = id("undo_blocks");
            public static S2CUndoBlocksPayload decode(PacketByteBuf byteBuf) {
                if (byteBuf.readableBytes() <= 0) return new S2CUndoBlocksPayload(List.of());
                List<BlockPos> out = new ArrayList<>();
                while (byteBuf.readableBytes() > 0) {
                    out.add(byteBuf.readBlockPos());
                }
                return new S2CUndoBlocksPayload(out);
            }

            public static PacketByteBuf encode(S2CUndoBlocksPayload data) {
                if (data.blockPosList.isEmpty()) return PacketByteBufs.empty();
                var buf = PacketByteBufs.create();
                for (BlockPos blockPos : data.blockPosList) {
                    buf.writeBlockPos(blockPos);
                }
                return buf;
            }

            public static Identifier getId() {
                return ID;
            }
        }

        public record C2SQueryUndoPayload(boolean undoPressed) {
            public static final Identifier ID = id("query_undo");

            public static PacketByteBuf encode(C2SQueryUndoPayload input) {
                var buf = PacketByteBufs.create();
                buf.writeBoolean(input.undoPressed());
                return buf;
            }

            public static C2SQueryUndoPayload decode(PacketByteBuf input) {
                return new C2SQueryUndoPayload(input.readBoolean());
            }

            public static Identifier getId() {
                return ID;
            }
        }

        public record C2SWandOptionPayload(String key, String value, boolean shouldNotify) {
            public static final Identifier ID = id("wand_option");

            public static PacketByteBuf encode(C2SWandOptionPayload input) {
                var buf = PacketByteBufs.create();
                buf.writeString(input.key());
                buf.writeString(input.value());
                buf.writeBoolean(input.shouldNotify());
                return buf;
            }

            public static C2SWandOptionPayload decode(PacketByteBuf input) {
                String key = input.readString();
                String value = input.readString();
                boolean shouldNotify = input.readBoolean();
                return new C2SWandOptionPayload(key, value, shouldNotify);
            }

            public static Identifier getId() {
                return ID;
            }

            public static C2SWandOptionPayload of(IOption<?> option, boolean notify) {
                return new C2SWandOptionPayload(option.getKey(), option.getValueString(), notify);
            }
        }

        public record S2CPing(boolean unused) {
            public static final Identifier ID = id("pong");

            public static PacketByteBuf encode(S2CPing input) {
                var buf = PacketByteBufs.create();
                buf.writeBoolean(input.unused());
                return buf;
            }

            public static S2CPing decode(PacketByteBuf input) {
                return new S2CPing(input.readBoolean());
            }

            public static Identifier getId() {
                return ID;
            }
        }

        public record C2SPong(boolean unused) {
            public static final Identifier ID = id("pong");

            public static PacketByteBuf encode(C2SPong input) {
                var buf = PacketByteBufs.create();
                buf.writeBoolean(input.unused());
                return buf;
            }

            public static C2SPong decode(PacketByteBuf input) {
                return new C2SPong(input.readBoolean());
            }

            public static Identifier getId() {
                return ID;
            }
        }

        public record S2CSyncModConfigPayload(List<Integer> ints, List<Boolean> booleans, List<String> similarBlocks, List<String> blockEntityList, List<WandConfigEntry> wands) {
            public static final Identifier ID = id("mod_config");

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

            public static PacketByteBuf encode(S2CSyncModConfigPayload input) {
                var b = PacketByteBufs.create();
                int[] lens = new int[] {input.ints.size(), input.booleans.size(), input.similarBlocks.size(), input.blockEntityList.size(), input.wands.size() };
                b.writeIntArray(lens);
                for (Integer anInt : input.ints) {
                    b.writeInt(anInt);
                }
                for (Boolean aBoolean : input.booleans) {
                    b.writeBoolean(aBoolean);
                }
                for (String similarBlock : input.similarBlocks) {
                    b.writeString(similarBlock);
                }
                for (String s : input.blockEntityList) {
                    b.writeString(s);
                }
                for (WandConfigEntry wand : input.wands) {
                    WandConfigEntry.encode(b, wand);
                }
                return b;
            }

            public static S2CSyncModConfigPayload decode(PacketByteBuf input) {
                var lens = input.readIntArray();
                // ints
                var ints = new ArrayList<Integer>();
                for (int i = 0; i < lens[0]; i++) {
                    ints.add(input.readInt());
                }
                // bools
                var booleans = new ArrayList<Boolean>();
                for (int i = 0; i < lens[1]; i++) {
                    booleans.add(input.readBoolean());
                }
                // similarBlocks
                var similarBlocks = new ArrayList<String>();
                for (int i = 0; i < lens[2]; i++) {
                    similarBlocks.add(input.readString());
                }
                // blockEntityList
                var blockEntityList = new ArrayList<String>();
                for (int i = 0; i < lens[3]; i++) {
                    blockEntityList.add(input.readString());
                }
                // wands
                var wands = new ArrayList<WandConfigEntry>();
                for (int i = 0; i < lens[4]; i++) {
                    wands.add(WandConfigEntry.decode(input));
                }
                return new S2CSyncModConfigPayload(ints, booleans, similarBlocks, blockEntityList, wands);
            }

            public static Identifier getId() {
                return ID;
            }
        }
    }

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SQueryUndoPayload.getId(), (MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {
            var payload = Payloads.C2SQueryUndoPayload.decode(buf);
            server.execute(() -> {
                if(player == null) return;
                UndoHistory.updateClient(player, payload.undoPressed());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Payloads.C2SWandOptionPayload.getId(), (MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {
            var payload = Payloads.C2SWandOptionPayload.decode(buf);
            server.execute(() -> {
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
            if (server.isDedicated()) ServerPlayNetworking.send(handler.player,
                    Payloads.S2CSyncModConfigPayload.getId(),
                    Payloads.S2CSyncModConfigPayload.encode(Payloads.S2CSyncModConfigPayload.create()));
            else ConstructionWand.ensureConfigManager();
        });
    }
}
