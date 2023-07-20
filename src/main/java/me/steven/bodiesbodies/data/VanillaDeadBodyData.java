package me.steven.bodiesbodies.data;

import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.entity.DeadBodyEntity;
import me.steven.bodiesbodies.screen.VanillaDeadBodyInventoryScreenHandler;
import me.steven.bodiesbodies.utils.Utils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

public class VanillaDeadBodyData implements DeadBodyData {
    public DefaultedList<ItemStack> main;
    public DefaultedList<ItemStack> armor;
    public DefaultedList<ItemStack> offHand;
    public int selectedSlot;
    public VanillaDeadBodyData() {
        this.main = DefaultedList.ofSize(36, ItemStack.EMPTY);
        this.armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
        this.offHand = DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Override
    public DeadBodyData transferFrom(PlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        main = DefaultedList.ofSize(inv.main.size(), ItemStack.EMPTY);
        for (int i = 0; i < inv.main.size(); i++) {
            ItemStack stack = inv.main.get(i);
            main.set(i, stack.copyAndEmpty());
        }

        armor = DefaultedList.ofSize(inv.armor.size(), ItemStack.EMPTY);
        for (int i = 0; i < inv.armor.size(); i++) {
            ItemStack stack = inv.armor.get(i);
            armor.set(i, stack.copyAndEmpty());
        }

        offHand = DefaultedList.ofSize(inv.offHand.size(), ItemStack.EMPTY);
        for (int i = 0; i < inv.offHand.size(); i++) {
            ItemStack stack = inv.offHand.get(i);
            offHand.set(i, stack.copyAndEmpty());
        }

        selectedSlot = inv.selectedSlot;

        return this;
    }

    @Override
    public String getId() {
        return "vanilla";
    }

    @Override
    public NbtCompound write(NbtCompound nbt) {
        nbt.put("main", write(main));
        nbt.put("armor", write(armor));
        nbt.put("offhand", write(offHand));
        return nbt;
    }

    @Override
    public void read(NbtCompound nbt) {
        read(main, nbt.getCompound("main"));
        read(armor, nbt.getCompound("armor"));
        read(offHand, nbt.getCompound("offhand"));
    }

    @Override
    public void transferTo(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            PlayerInventory inv = player.getInventory();
            for (int i = 0; i < inv.main.size(); i++) {
                offer(inv.main, entity.getWorld(), entity.getBlockPos(), i, main.get(i));
            }
            for (int i = 0; i < inv.armor.size(); i++) {
                offer(inv.armor, entity.getWorld(), entity.getBlockPos(), i, armor.get(i));
            }
            for (int i = 0; i < inv.offHand.size(); i++) {
                offer(inv.offHand, entity.getWorld(), entity.getBlockPos(), i, offHand.get(i));
            }
        } else if (entity instanceof SkeletonEntity skeleton) {
            skeleton.equipStack(EquipmentSlot.HEAD, armor.get(3));
            skeleton.equipStack(EquipmentSlot.CHEST, armor.get(2));
            skeleton.equipStack(EquipmentSlot.LEGS, armor.get(1));
            skeleton.equipStack(EquipmentSlot.FEET, armor.get(0));
            skeleton.equipStack(EquipmentSlot.MAINHAND, main.get(selectedSlot));
            skeleton.equipStack(EquipmentSlot.OFFHAND, offHand.get(0));
        }
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player, DeathData data) {
        return new VanillaDeadBodyInventoryScreenHandler(syncId, playerInventory, data, this);
    }

    @Override
    public boolean isEmpty() {
        return Utils.isEmpty(main) && Utils.isEmpty(offHand) && Utils.isEmpty(armor);
    }

    @Override
    public DeadBodyData deepCopy() {
        VanillaDeadBodyData data = new VanillaDeadBodyData();
        for (int i = 0; i < data.main.size(); i++) {
            data.main.set(i, main.get(i).copy());
        }

        for (int i = 0; i < data.armor.size(); i++) {
            data.armor.set(i, armor.get(i).copy());
        }

        for (int i = 0; i < data.offHand.size(); i++) {
            data.offHand.set(i, offHand.get(i).copy());
        }
        return data;
    }
}
