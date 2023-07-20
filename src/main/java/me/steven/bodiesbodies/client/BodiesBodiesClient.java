package me.steven.bodiesbodies.client;

import me.steven.bodiesbodies.BodiesBodies;
import me.steven.bodiesbodies.compat.TrinketClientCompat;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.entity.DeadBodyEntityRenderer;
import me.steven.bodiesbodies.screen.DeathHistoryScreen;
import me.steven.bodiesbodies.screen.VanillaDeadBodyInventoryScreen;
import me.steven.bodiesbodies.screen.VanillaDeadBodyInventoryScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BodiesBodiesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(BodiesBodies.DEAD_BODY_ENTITY_TYPE, DeadBodyEntityRenderer::new);

        HandledScreens.register(BodiesBodies.VANILLA_DEAD_BODY_SH, new HandledScreens.Provider<VanillaDeadBodyInventoryScreenHandler, VanillaDeadBodyInventoryScreen>() {
            @Override
            public VanillaDeadBodyInventoryScreen create(VanillaDeadBodyInventoryScreenHandler handler, PlayerInventory playerInventory, Text title) {
                return new VanillaDeadBodyInventoryScreen(handler, playerInventory, title, handler.deathData);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(BodiesBodies.OPEN_DEATH_HISTORY, (client, handler, buf, responseSender) -> {
            int size = buf.readInt();
            List<DeathData> deathData = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                deathData.add(null);
            }

            for (int i = 0; i < size; i++) {
                deathData.set(buf.readInt(), DeathData.readNbt(buf.readNbt()));
            }

            client.execute(() -> {
                client.setScreen(new DeathHistoryScreen(Text.literal("Death History"), deathData));
            });
        });

        if (FabricLoader.getInstance().isModLoaded("trinkets"))
            TrinketClientCompat.loadClient();
    }
}
