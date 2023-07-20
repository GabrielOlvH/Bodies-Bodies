package me.steven.bodiesbodies.compat;

import dev.emi.trinkets.TrinketScreenManager;
import me.steven.bodiesbodies.BodiesBodies;
import me.steven.bodiesbodies.data.DeadBodyData;
import me.steven.bodiesbodies.data.DeadBodyDataProvider;
import me.steven.bodiesbodies.data.VanillaDeadBodyData;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.entity.DeadBodyEntity;
import me.steven.bodiesbodies.screen.VanillaDeadBodyInventoryScreen;
import me.steven.bodiesbodies.screen.VanillaDeadBodyInventoryScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TrinketCompat {
    public static final ScreenHandlerType<TrinketsDeadBodyInventoryScreenHandler> TRINKETS_DEAD_BODY_SH = Registry.register(Registries.SCREEN_HANDLER, new Identifier("bodiesbodies", "trinkets_dead_body"), new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> {
        int id = buf.readInt();
        DeathData deathData = DeathData.readNbt(buf.readNbt());
        for (DeadBodyData data : deathData.savedData()) {
            if (data instanceof TrinketsDeadBodyData trinketsDeadBodyData) {
                return new TrinketsDeadBodyInventoryScreenHandler(syncId, inventory, deathData, trinketsDeadBodyData);
            }
        }
        return null;
    }));

    public static void load() {
        DeadBodyDataProvider.register(TrinketsDeadBodyData::new);
    }
}
