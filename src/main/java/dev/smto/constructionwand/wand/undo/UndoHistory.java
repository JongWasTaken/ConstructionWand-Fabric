package dev.smto.constructionwand.wand.undo;

import dev.smto.constructionwand.ConstructionWand;
import dev.smto.constructionwand.Network;
import dev.smto.constructionwand.integrations.mod.ModCompat;
import dev.smto.constructionwand.integrations.polymer.PolymerManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.*;
import java.util.stream.Collectors;

public class UndoHistory
{
    private static final HashMap<UUID, PlayerEntityEntry> history = new HashMap<>();

    private static PlayerEntityEntry getEntryFromPlayerEntity(Player player) {
        return history.computeIfAbsent(player.getUUID(), k -> new PlayerEntityEntry());
    }

    public static void add(Player player, Level world, List<ISnapshot> placeSnapshots) {
        LinkedList<HistoryEntry> list = getEntryFromPlayerEntity(player).entries;
        list.add(new HistoryEntry(placeSnapshots, world));
        while(list.size() > ConstructionWand.Config.undoHistorySize) list.removeFirst();
    }

    public static void removePlayerEntity(Player player) {
        history.remove(player.getUUID());
    }

    public static void updateClient(Player player, boolean ctrlDown) {
        Level world = player.level();
        if(world.isClientSide()) return;

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

        ServerPlayNetworking.send((ServerPlayer) player, new Network.Payloads.S2CUndoBlocksPayload(positions.stream().toList()));
    }

    public static boolean isUndoActive(Player player) {
        if (ModCompat.polymerEnabled) {
            if (PolymerManager.hasClientMod(player.getUUID())) return getEntryFromPlayerEntity(player).undoActive;
            return player.isShiftKeyDown();
        } else return getEntryFromPlayerEntity(player).undoActive;
    }

    public static boolean undo(Player player, Level world, BlockPos pos) {
        PlayerEntityEntry playerEntry = getEntryFromPlayerEntity(player);

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

    private record HistoryEntry(List<ISnapshot> placeSnapshots, Level world) {

        public Set<BlockPos> getBlockPositions() {
                return placeSnapshots.stream().map(ISnapshot::getPos).collect(Collectors.toSet());
            }

            public boolean withinRange(BlockPos pos) {
                Set<BlockPos> positions = getBlockPositions();

                if (positions.contains(pos)) return true;

                for (BlockPos p : positions) {
                    if (pos.closerThan(p, 3)) return true;
                }
                return false;
            }

            public boolean undo(Player player) {
                // Check first if all snapshots can be restored
                for (ISnapshot snapshot : placeSnapshots) {
                    if (!snapshot.canRestore(world, player)) return false;
                }
                for (ISnapshot snapshot : placeSnapshots) {
                    if (snapshot.restore(world, player) && !player.isCreative()) {
                        for (int i = 0; i < snapshot.getRequiredItems().size(); i++) {
                            if (i == 0 || snapshot.shouldGiveBackIncludedItem()) {
                                player.handleExtraItemsCreatedOnUse(snapshot.getRequiredItems().get(i));
                            }
                        }
                    }
                }
                player.getInventory().setChanged();

                // Play teleport sound
                SoundEvent sound = SoundEvents.CHORUS_FRUIT_TELEPORT;
                world.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);

                return true;
            }
        }
}
