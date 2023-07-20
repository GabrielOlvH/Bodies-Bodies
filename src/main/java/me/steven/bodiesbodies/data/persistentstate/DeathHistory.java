package me.steven.bodiesbodies.data.persistentstate;

import me.steven.bodiesbodies.data.DeadBodyData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DeathHistory extends PersistentState {


    private final ServerWorld world;
    private int deathCounter = 0;
    private final Map<UUID, List<DeathData>> storedDeaths = new HashMap<>();
    private final Map<Integer, PlayerBackup> playerNbtBackup = new HashMap<>();

    public DeathHistory(ServerWorld world) {
        this.world = world;
    }

    public List<DeathData> getDeathsFor(UUID uuid) {
        return storedDeaths.get(uuid);
    }


    public Map<Integer, PlayerBackup> getPlayerNbtBackup() {
        return playerNbtBackup;
    }

    public void removeDeathData(UUID uuid, int id) {
        storedDeaths.get(uuid).removeIf(d -> d.id() == id);
        playerNbtBackup.remove(id);
        markDirty();
    }

    @Nullable
    public DeathData getDeathData(UUID uuid, int id) {
        if (!storedDeaths.containsKey(uuid)) return null;

        for (DeathData deathData : storedDeaths.get(uuid)) {
            if (deathData.id() == id) return deathData;
        }
        return null;
    }

    public int save(int id, ServerPlayerEntity player, BlockPos pos, List<DeadBodyData> data) {
        DeathData deathData = new DeathData(id, pos, player.getWorld().getDimensionKey().getValue(), data, System.currentTimeMillis());
        List<DeathData> deaths = storedDeaths.computeIfAbsent(player.getUuid(), (x) -> new ArrayList<>());
        deaths.add(deathData);
        markDirty();
        return deathData.id();
    }

    public int backup(ServerPlayerEntity player) {
        int id = deathCounter++;
        playerNbtBackup.put(id, new PlayerBackup(player.writeNbt(new NbtCompound()), System.currentTimeMillis()));
        markDirty();
        return id;
    }

    public int purgeOldEntries(DeathHistory purged) {
        AtomicInteger i = new AtomicInteger(0);
        playerNbtBackup.entrySet().removeIf(e -> {
            boolean remove = e.getValue().createdTime() + TimeUnit.DAYS.toMillis(7) < System.currentTimeMillis();
            if (remove) {
                i.getAndIncrement();
                purged.playerNbtBackup.put(e.getKey(), e.getValue());
            }
            return remove;
        });
        storedDeaths.forEach((uuid, deaths) -> deaths.removeIf(d -> {
            boolean remove = d.createdTime() + TimeUnit.DAYS.toMillis(7) < System.currentTimeMillis();
            if (remove) {
                i.getAndIncrement();
                purged.storedDeaths.computeIfAbsent(uuid, (x) -> new ArrayList<>()).add(d);
            }
            return remove;
        }));
        if (i.get()>0) {
            System.out.println("Moved 1 week old entries to death_history_purged.dat");
            markDirty();
        }
        return i.get();

    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {

        NbtCompound rawPlayerNbt = new NbtCompound();
        playerNbtBackup.forEach((id, playerNbt) -> {
            rawPlayerNbt.put(id.toString(), playerNbt.writeNbt());

        });

        nbt.put("RawNbt", rawPlayerNbt);
        nbt.putInt("deathCounter", deathCounter);
        NbtCompound deathsNbt = new NbtCompound();
        for (Map.Entry<UUID, List<DeathData>> entry : storedDeaths.entrySet()) {
            UUID uuid = entry.getKey();
            List<DeathData> deaths = entry.getValue();
            NbtList list = new NbtList();
            for (DeathData death : deaths) {
                list.add(death.writeNbt());
            }

            deathsNbt.put(uuid.toString(), list);
        }
        nbt.put("deaths", deathsNbt);


        return nbt;
    }

    public static DeathHistory readNbt(ServerWorld world, NbtCompound nbt) {
        DeathHistory data = new DeathHistory(world);

        NbtCompound rawNbt = nbt.getCompound("RawNbt");
        for (String key : rawNbt.getKeys()) {
            int id = Integer.parseInt(key);
            data.playerNbtBackup.put(id, PlayerBackup.readNbt(rawNbt.getCompound(key)));
        }

        data.deathCounter = nbt.getInt("deathCounter");
        NbtCompound deathsNbt = nbt.getCompound("deaths");
        for (String uuidAsString : deathsNbt.getKeys()) {
            UUID uuid = UUID.fromString(uuidAsString);
            List<DeathData> deaths = new ArrayList<>();
            NbtList list = deathsNbt.getList(uuidAsString, 10);
            for (NbtElement element : list) {
                deaths.add(DeathData.readNbt((NbtCompound) element));
            }
            data.storedDeaths.put(uuid, deaths);
        }

        return data;
    }

    public static DeathHistory getState(ServerWorld world) {
        return world.getServer().getOverworld().getPersistentStateManager()
                .getOrCreate(nbt -> DeathHistory.readNbt(world, nbt), () -> new DeathHistory(world), "death_history");
    }
}
