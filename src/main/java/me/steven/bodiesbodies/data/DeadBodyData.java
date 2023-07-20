package me.steven.bodiesbodies.data;

import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.entity.DeadBodyEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface DeadBodyData {

    DeadBodyData transferFrom(PlayerEntity player);

    void transferTo(LivingEntity entity);

    String getId();

    NbtCompound write(NbtCompound nbt);

    void read(NbtCompound nbt);

    ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player, DeathData data);

    boolean isEmpty();

    DeadBodyData deepCopy();

    default NbtCompound write(DefaultedList<ItemStack> stacks) {
        NbtCompound nbt = new NbtCompound();
        NbtList inv = new NbtList();
        for (int i = 0; i < stacks.size(); i++) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putInt("Slot", i);
            stacks.get(i).writeNbt(nbtCompound);
            inv.add(nbtCompound);
        }

        nbt.put("Stacks", inv);
        return nbt;
    }

    default void read(DefaultedList<ItemStack> stacks, NbtCompound nbt) {
        NbtList inv = nbt.getList("Stacks", 10);
        for (int i = 0; i < inv.size(); i++) {
            NbtCompound nbtCompound = inv.getCompound(i);
            int slot = nbtCompound.getInt("Slot");
            stacks.set(slot, ItemStack.fromNbt(nbtCompound));
        }
    }

    default void offer(DefaultedList<ItemStack> stacks, World world, BlockPos pos, int i, ItemStack stack) {
        if (!stacks.get(i).isEmpty()) {
            int firstEmpty = getFirstEmptyIndex(stacks);
            if (firstEmpty != -1) {
                stacks.set(firstEmpty, stack.copyAndEmpty());
            } else {
                ItemScatterer.spawn(world, pos, DefaultedList.ofSize(1, stack.copyAndEmpty()));
            }
        } else {
            stacks.set(i, stack.copyAndEmpty());
        }
    }

    default int getFirstEmptyIndex(DefaultedList<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            if (stacks.get(i).isEmpty()) return i;
        }
        return -1;
    }

}
