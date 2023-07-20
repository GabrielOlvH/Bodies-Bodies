package me.steven.bodiesbodies.screen;

import com.mojang.datafixers.util.Pair;
import me.steven.bodiesbodies.BodiesBodies;
import me.steven.bodiesbodies.data.VanillaDeadBodyData;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.entity.DeadBodyEntity;
import me.steven.bodiesbodies.utils.Utils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import static net.minecraft.screen.PlayerScreenHandler.*;

public class VanillaDeadBodyInventoryScreenHandler extends ScreenHandler {
    private static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{EMPTY_BOOTS_SLOT_TEXTURE, EMPTY_LEGGINGS_SLOT_TEXTURE, EMPTY_CHESTPLATE_SLOT_TEXTURE, EMPTY_HELMET_SLOT_TEXTURE};
    private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public final DeathData deathData;
    public VanillaDeadBodyInventoryScreenHandler(int syncId, PlayerInventory inventory, DeathData deathData, VanillaDeadBodyData data) {
        super(BodiesBodies.VANILLA_DEAD_BODY_SH, syncId);
        this.deathData = deathData;

        SimpleInventory armorInv = Utils.toSimpleInventory(data.armor);
        for(int i = 0; i < 4; ++i) {
            final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
            this.addSlot(new Slot(armorInv, 3-i, 8 + i* 18, 23) {

                public int getMaxItemCount() {
                    return 1;
                }

                public boolean canInsert(ItemStack stack) {
                    return false;
                }

                public Pair<Identifier, Identifier> getBackgroundSprite() {
                    return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getEntitySlotId()]);
                }
            });
        }
        SimpleInventory mainInv = Utils.toSimpleInventory(data.main);

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(mainInv, j + (i + 1) * 9, 8 + j * 18, 45 + i * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false;
                    }
                });
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(mainInv, i, 8 + i * 18, 103){
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }
            });
        }

        SimpleInventory offHandInv = Utils.toSimpleInventory(data.offHand);

        this.addSlot(new Slot(offHandInv, 0, 98, 23) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT);
            }
        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + (i + 1) * 9, 8 + j * 18, 142 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 200));
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
