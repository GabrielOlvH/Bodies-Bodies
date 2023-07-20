package me.steven.bodiesbodies.utils;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class Utils {
    public static SimpleInventory toSimpleInventory(DefaultedList<ItemStack> stacks) {
        SimpleInventory inv = new SimpleInventory(stacks.size());
        for (int i = 0; i < inv.size(); i++) {
            inv.setStack(i, stacks.get(i));
        }
        return inv;
    }
    public static boolean isEmpty(DefaultedList<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }
}
