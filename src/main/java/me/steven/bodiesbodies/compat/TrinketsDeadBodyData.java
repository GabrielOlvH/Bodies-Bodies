package me.steven.bodiesbodies.compat;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import me.steven.bodiesbodies.data.DeadBodyData;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.entity.DeadBodyEntity;
import me.steven.bodiesbodies.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Map;

public class TrinketsDeadBodyData implements DeadBodyData {
    public Map<String, Map<String, DefaultedList<ItemStack>>> inventory = new HashMap<>();
    @Override
    public DeadBodyData transferFrom(PlayerEntity player) {
        TrinketsApi.getTrinketComponent(player).map(TrinketComponent::getInventory).ifPresent(inv -> {
            inv.forEach((key, value) -> {
                Map<String, DefaultedList<ItemStack>> aaa = new HashMap<>();
                value.forEach((slot, trinketInv) -> {
                    int size = trinketInv.size();
                    DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
                    for (int i = 0; i < size; i++) {
                        stacks.set(i, trinketInv.getStack(i).copyAndEmpty());
                    }
                    aaa.put(slot, stacks);
                });
                inventory.put(key, aaa);
            });
        });
        return this;
    }


    @Override
    public void transferTo(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            TrinketsApi.getTrinketComponent(player).map(TrinketComponent::getInventory).ifPresent(inv -> {
                inventory.forEach((key, value) -> {
                    value.forEach((slot, stacks) -> {
                        for (int i = 0; i < stacks.size(); i++) {
                            offer(inv.get(key).get(slot), entity.getWorld(), entity.getBlockPos(), i, stacks.get(i).copyAndEmpty(), player.getInventory().main);
                        }
                    });
                });
            });
        }
    }

    private void offer(TrinketInventory stacks, World world, BlockPos pos, int i, ItemStack stack, DefaultedList<ItemStack> fallback) {
        if (!stacks.getStack(i).isEmpty()) {
            int firstEmpty = getFirstEmptyIndex(fallback);
            if (firstEmpty != -1) {
                fallback.set(firstEmpty, stack.copyAndEmpty());
            } else {
                ItemScatterer.spawn(world, pos, DefaultedList.ofSize(1, stack.copyAndEmpty()));
            }
        } else {
            stacks.setStack(i, stack.copyAndEmpty());
        }
    }

    @Override
    public String getId() {
        return "trinkets";
    }

    @Override
    public NbtCompound write(NbtCompound nbt) {
        NbtCompound trinketNbt = new NbtCompound();
        inventory.forEach((key, value) -> {
            NbtCompound nbtKey = new NbtCompound();
            value.forEach((slot, stacks) -> nbtKey.put(slot, write(stacks)));
            trinketNbt.put(key, nbtKey);
        });
        nbt.put("TrinketData", trinketNbt);
        return nbt;
    }

    @Override
    public void read(NbtCompound nbt) {
        NbtCompound trinketNbt = nbt.getCompound("TrinketData");
        for (String key : trinketNbt.getKeys()) {
            NbtCompound nbtKey = trinketNbt.getCompound(key);
            Map<String, DefaultedList<ItemStack>> map = new HashMap<>();
            for (String slot : nbtKey.getKeys()) {
                DefaultedList<ItemStack> stacks = DefaultedList.ofSize(nbtKey.getCompound(slot).getSize(), ItemStack.EMPTY);
                read(stacks, nbtKey.getCompound(slot));
                map.put(slot, stacks);
            }
            inventory.put(key, map);
        }
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player, DeathData data) {
        return new TrinketsDeadBodyInventoryScreenHandler(syncId, playerInventory, data, this);
    }

    @Override
    public boolean isEmpty() {
        return inventory.values().stream().allMatch((e) -> e.values().stream().allMatch(Utils::isEmpty));
    }

    @Override
    public DeadBodyData deepCopy() {
        Map<String, Map<String, DefaultedList<ItemStack>>> copy = new HashMap<>();
        inventory.forEach((key, value) -> {
            Map<String, DefaultedList<ItemStack>> aaa = new HashMap<>();
            value.forEach((slot, stacks) -> {
                DefaultedList<ItemStack> stacksCopy = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);
                for (int i = 0; i < stacks.size(); i++) {
                    stacksCopy.set(i, stacks.get(i).copy());
                }
                aaa.put(slot, stacksCopy);
            });
            copy.put(key, aaa);
        });
        TrinketsDeadBodyData copyData = new TrinketsDeadBodyData();
        copyData.inventory = copy;
        return copyData;
    }
}
