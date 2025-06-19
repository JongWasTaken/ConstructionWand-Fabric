package pw.smto.constructionwand.wand.undo;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pw.smto.constructionwand.ConstructionWand;
import pw.smto.constructionwand.Network;

import java.util.*;
import java.util.stream.Collectors;

public class UndoHistory
{
    private static final HashMap<UUID, PlayerEntityEntry> history = new HashMap<>();

    private static PlayerEntityEntry getEntryFromPlayerEntity(PlayerEntity player) {
        return history.computeIfAbsent(player.getUuid(), k -> new PlayerEntityEntry());
    }

    public static void add(PlayerEntity player, World world, List<ISnapshot> placeSnapshots) {
        LinkedList<HistoryEntry> list = getEntryFromPlayerEntity(player).entries;
        list.add(new HistoryEntry(placeSnapshots, world));
        while(list.size() > ConstructionWand.Config.undoHistorySize) list.removeFirst();
    }

    public static void removePlayerEntity(PlayerEntity player) {
        history.remove(player.getUuid());
    }

    public static void updateClient(PlayerEntity player, boolean ctrlDown) {
        World world = player.getWorld();
        if(world.isClient) return;

        // Set state of CTRL key
        PlayerEntityEntry playerEntry = getEntryFromPlayerEntity(player);
        playerEntry.undoActive = ctrlDown;

        LinkedList<HistoryEntry> historyEntries = playerEntry.entries;
        Set<BlockPos> positions;

        // Send block positions of most recent entry to client
        if(historyEntries.isEmpty()) positions = Collections.emptySet();
        else {
            HistoryEntry entry = historyEntries.getLast();

            if(entry == null || !entry.world.equals(world)) positions = Collections.emptySet();
            else positions = entry.getBlockPositions();
        }

        ServerPlayNetworking.send((ServerPlayerEntity) player, new Network.Payloads.S2CUndoBlocksPayload(positions.stream().toList()));
    }

    public static boolean isUndoActive(PlayerEntity player) {
        return getEntryFromPlayerEntity(player).undoActive;
    }

    public static boolean undo(PlayerEntity player, World world, BlockPos pos) {
        // If CTRL key is not pressed, return
        PlayerEntityEntry playerEntry = getEntryFromPlayerEntity(player);
        if(!playerEntry.undoActive) return false;

        // Get the most recent entry for undo
        LinkedList<HistoryEntry> historyEntries = playerEntry.entries;
        if(historyEntries.isEmpty()) return false;
        HistoryEntry entry = historyEntries.getLast();

        // PlayerEntity has to be in the same world and near the blocks
        if(!entry.world.equals(world) || !entry.withinRange(pos)) return false;

        if(entry.undo(player)) {
            historyEntries.remove(entry);
            updateClient(player, true);
            return true;
        }
        return false;
    }

    private static class PlayerEntityEntry
    {
        public final LinkedList<HistoryEntry> entries;
        public boolean undoActive;

        public PlayerEntityEntry() {
            entries = new LinkedList<>();
            undoActive = false;
        }
    }

    private record HistoryEntry(List<ISnapshot> placeSnapshots, World world) {

        public Set<BlockPos> getBlockPositions() {
                return placeSnapshots.stream().map(ISnapshot::getPos).collect(Collectors.toSet());
            }

            public boolean withinRange(BlockPos pos) {
                Set<BlockPos> positions = getBlockPositions();

                if (positions.contains(pos)) return true;

                for (BlockPos p : positions) {
                    if (pos.isWithinDistance(p, 3)) return true;
                }
                return false;
            }

            public boolean undo(PlayerEntity player) {
                // Check first if all snapshots can be restored
                for (ISnapshot snapshot : placeSnapshots) {
                    if (!snapshot.canRestore(world, player)) return false;
                }
                for (ISnapshot snapshot : placeSnapshots) {
                    if (snapshot.restore(world, player) && !player.isCreative()) {
                        for (int i = 0; i < snapshot.getRequiredItems().size(); i++) {
                            if (i == 0 || snapshot.shouldGiveBackIncludedItem()) {
                                if (!player.giveItemStack(snapshot.getRequiredItems().get(i))) {
                                    player.dropItem(snapshot.getRequiredItems().get(i), false);
                                }
                            }
                        }
                    }
                }
                player.getInventory().markDirty();

                // Play teleport sound
                SoundEvent sound = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                world.playSound(null, player.getBlockPos(), sound, SoundCategory.PLAYERS, 1.0F, 1.0F);

                return true;
            }
        }
}
