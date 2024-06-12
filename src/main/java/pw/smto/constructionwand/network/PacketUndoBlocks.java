package pw.smto.constructionwand.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class PacketUndoBlocks
{
    public HashSet<BlockPos> undoBlocks;

    public PacketUndoBlocks(Set<BlockPos> undoBlocks) {
        this.undoBlocks = new HashSet<>(undoBlocks);
    }

    private PacketUndoBlocks(HashSet<BlockPos> undoBlocks) {
        this.undoBlocks = undoBlocks;
    }

    public static void encode(PacketUndoBlocks msg, PacketByteBuf buffer) {
        for(BlockPos pos : msg.undoBlocks) {
            buffer.writeBlockPos(pos);
        }
    }

    public static PacketUndoBlocks decode(PacketByteBuf buffer) {
        HashSet<BlockPos> undoBlocks = new HashSet<>();

        while(buffer.isReadable()) {
            undoBlocks.add(buffer.readBlockPos());
        }
        return new PacketUndoBlocks(undoBlocks);
    }
}
