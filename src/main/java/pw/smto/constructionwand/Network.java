package pw.smto.constructionwand;

import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import pw.smto.constructionwand.basics.WandUtil;
import pw.smto.constructionwand.basics.option.IOption;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.items.wand.ItemWand;

import java.util.List;

import static pw.smto.constructionwand.ConstructionWand.MOD_ID;

public class Network {
    public static class Channels {
        public static final OwoNetChannel S2C_UNDO_BLOCKS = OwoNetChannel.create(new Identifier(MOD_ID, "undo_blocks"));
        public static final OwoNetChannel C2S_QUERY_UNDO = OwoNetChannel.create(new Identifier(MOD_ID, "query_undo"));
        public static final OwoNetChannel C2S_WAND_OPTION = OwoNetChannel.create(new Identifier(MOD_ID, "wand_option"));

    }
    public static class PacketData {
        public record None() {}
        public record UndoBlocks(List<BlockPos> undoBlocks) {}
        public record WandOption(String key, String value, boolean notify1) {
            public static WandOption of(IOption<?> option, boolean notify) {
                return new WandOption(option.getKey(), option.getValueString(), notify);
            }
        }
        public record QueryUndo(boolean undoPressed) {};
    }

    public static void init() {
        Channels.S2C_UNDO_BLOCKS.registerClientboundDeferred(PacketData.UndoBlocks.class);
        Channels.C2S_WAND_OPTION.registerServerbound(PacketData.WandOption.class, (message, access) -> {
            if (access.runtime() == null) return;

            ServerPlayerEntity player = access.player();
            if(player == null) return;

            ItemStack wand = WandUtil.holdingWand(player);
            if(wand == null) return;
            WandOptions options = new WandOptions(wand);

            IOption<?> option = options.get(message.key);
            if(option == null) return;
            option.setValueString(message.value);

            if(message.notify1) ItemWand.optionMessage(player, option);
            player.getInventory().markDirty();
        });
        Channels.C2S_QUERY_UNDO.registerServerbound(PacketData.QueryUndo.class, (message, access) -> {
            if (access.runtime() == null) return;

            ServerPlayerEntity player = access.player();
            if(player == null) return;

            ConstructionWand.undoHistory.updateClient(player, message.undoPressed);
        });
    }

    public static void sendPacket(PlayerEntity target, OwoNetChannel channel) {
        channel.serverHandle((ServerPlayerEntity)target).send(new PacketData.None());
    }
    public static void sendPacket(ServerPlayerEntity target, OwoNetChannel channel) {
        channel.serverHandle(target).send(new PacketData.None());
    }
    public static void sendPacket(PlayerEntity target, OwoNetChannel channel, Record data) {
        channel.serverHandle((ServerPlayerEntity)target).send(data);
    }
    public static void sendPacket(ServerPlayerEntity target, OwoNetChannel channel, Record data) {
        channel.serverHandle(target).send(data);
    }
}
