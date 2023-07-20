package me.steven.bodiesbodies.compat;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class TrinketClientCompat {
    public static void loadClient() {
        HandledScreens.register(TrinketCompat.TRINKETS_DEAD_BODY_SH, new HandledScreens.Provider<TrinketsDeadBodyInventoryScreenHandler, TrinketsDeadBodyInventoryScreen>() {
            @Override
            public TrinketsDeadBodyInventoryScreen create(TrinketsDeadBodyInventoryScreenHandler handler, PlayerInventory playerInventory, Text title) {
                return new TrinketsDeadBodyInventoryScreen(handler, playerInventory, title, handler.deathData);
            }
        });
    }
}
