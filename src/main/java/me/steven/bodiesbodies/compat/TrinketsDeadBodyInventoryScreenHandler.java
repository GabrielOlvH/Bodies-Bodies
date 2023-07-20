package me.steven.bodiesbodies.compat;

import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.utils.Utils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TrinketsDeadBodyInventoryScreenHandler extends ScreenHandler {
    public final DeathData deathData;
    public TrinketsDeadBodyInventoryScreenHandler(int syncId, PlayerInventory inventory, DeathData deathData, TrinketsDeadBodyData data) {
        super(TrinketCompat.TRINKETS_DEAD_BODY_SH, syncId);
        this.deathData = deathData;

        List<SimpleInventory> inventories = data.inventory.values().stream().map(stringDefaultedListMap -> stringDefaultedListMap.values().stream().map(Utils::toSimpleInventory).toList()).flatMap(Collection::stream).toList();
        Iterator<SimpleInventory> it = inventories.iterator();
        if (!it.hasNext()) return;
        SimpleInventory current = it.next();
        int slotIndex = 0;
        outer:
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(current, slotIndex, 8 + j * 18, 28 + i * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false;
                    }
                });
                slotIndex++;
                if (current.size() <= slotIndex) {
                    if (!it.hasNext()) break outer;
                    current = it.next();
                }
            }
        }

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + (i + 1) * 9, 8 + j * 18, 94 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 152));
        }

    }

    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(itemStack);
            if (slot == 0) {
                if (!this.insertItem(itemStack2, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }

                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot >= 1 && slot < 5) {
                if (!this.insertItem(itemStack2, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= 5 && slot < 9) {
                if (!this.insertItem(itemStack2, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR && !((Slot)this.slots.get(8 - equipmentSlot.getEntitySlotId())).hasStack()) {
                int i = 8 - equipmentSlot.getEntitySlotId();
                if (!this.insertItem(itemStack2, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentSlot == EquipmentSlot.OFFHAND && !((Slot)this.slots.get(45)).hasStack()) {
                if (!this.insertItem(itemStack2, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= 9 && slot < 36) {
                if (!this.insertItem(itemStack2, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= 36 && slot < 45) {
                if (!this.insertItem(itemStack2, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 9, 45, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
            if (slot == 0) {
                player.dropItem(itemStack2, false);
            }
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
