package me.steven.bodiesbodies.data.persistentstate;

import me.steven.bodiesbodies.data.DeadBodyData;
import me.steven.bodiesbodies.data.DeadBodyDataProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DeathData(int id, BlockPos pos, Identifier dimension, List<DeadBodyData> savedData, long createdTime) {

    public NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("id", id);
        nbt.put("pos", NbtHelper.fromBlockPos(pos));
        nbt.putString("dim", dimension.toString());
        for (DeadBodyData data : savedData) {
            nbt.put(data.getId(), data.write(new NbtCompound()));
        }
        nbt.putLong("CreatedAt", createdTime);
        return nbt;
    }

    public static DeathData readNbt(NbtCompound nbt) {
        int id = nbt.getInt("id");
        BlockPos pos = NbtHelper.toBlockPos(nbt.getCompound("pos"));
        Identifier dimension = new Identifier(nbt.getString("dim"));
        List<DeadBodyData> savedData = new ArrayList<>(DeadBodyDataProvider.initEmpty());
        for (DeadBodyData data : savedData) {
            data.read(nbt.getCompound(data.getId()));
        }
        long createdTime = nbt.getLong("CreatedAt");
        return new DeathData(id, pos, dimension, savedData, createdTime);
    }
}
