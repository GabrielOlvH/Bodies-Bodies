package me.steven.bodiesbodies.data.persistentstate;

import net.minecraft.nbt.NbtCompound;

public record PlayerBackup(NbtCompound data, long createdTime) {
    public NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("Backup", this.data);
        nbt.putLong("CreatedAt", createdTime);
        return nbt;
    }

    public static PlayerBackup readNbt(NbtCompound nbt) {
        NbtCompound backup = nbt.getCompound("Backup");
        long createdTime = nbt.getLong("CreatedAt");
        return new PlayerBackup(backup, createdTime);
    }
}
