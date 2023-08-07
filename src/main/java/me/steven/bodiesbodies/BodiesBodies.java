package me.steven.bodiesbodies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.steven.bodiesbodies.compat.TrinketCompat;
import me.steven.bodiesbodies.data.DeadBodyData;
import me.steven.bodiesbodies.data.DeadBodyDataProvider;
import me.steven.bodiesbodies.data.VanillaDeadBodyData;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.data.persistentstate.DeathHistory;
import me.steven.bodiesbodies.entity.DeadBodyEntity;
import me.steven.bodiesbodies.screen.VanillaDeadBodyInventoryScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class BodiesBodies implements ModInitializer {

    public static final EntityType<DeadBodyEntity> DEAD_BODY_ENTITY_TYPE = Registry.register(Registries.ENTITY_TYPE, new Identifier("bodiesbodies", "dead_body"), EntityType.Builder.create(DeadBodyEntity::new, SpawnGroup.MISC).setDimensions(0.6F, 1.8F).build("dead_body"));

    public static final ScreenHandlerType<VanillaDeadBodyInventoryScreenHandler> VANILLA_DEAD_BODY_SH = Registry.register(Registries.SCREEN_HANDLER, new Identifier("bodiesbodies", "vanilla_dead_body"), new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> {
        int id = buf.readInt();
        DeathData deathData = DeathData.readNbt(buf.readNbt());
        for (DeadBodyData data : deathData.savedData()) {
            if (data instanceof VanillaDeadBodyData vanillaDeadBodyData) {
                return new VanillaDeadBodyInventoryScreenHandler(syncId, inventory, deathData, vanillaDeadBodyData);
            }
        }
        return null;
    }));

    public static final Identifier TRANSFER_ALL_ITEMS_PACKET = new Identifier("bodiesbodies", "transfer_all");
    public static final Identifier OPEN_DEAD_BODY_INV = new Identifier("bodiesbodies", "open_inv");
    public static final Identifier OPEN_DEATH_HISTORY = new Identifier("bodiesbodies", "death_history");

    @Override
    public void onInitialize() {
        File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "bodiesbodies.json");
        try {
            if (!file.exists()) {
                file.createNewFile();
                Files.writeString(file.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(Config.CONFIG));

            } else {
                Config.CONFIG = new GsonBuilder().create().fromJson(Files.readString(file.toPath(), Charset.defaultCharset()), Config.class);
            }
        } catch (IOException e) {
            Config.CONFIG = new Config();
            System.out.println("Error while loading Bodies! Bodies! config, loading default");
        }

        DeadBodyDataProvider.register(VanillaDeadBodyData::new);

        ServerPlayNetworking.registerGlobalReceiver(TRANSFER_ALL_ITEMS_PACKET, (server, player, handler, buf, responseSender) -> {
            int deathId = buf.readInt();
            server.execute(() -> {
                DeathData deathData = DeathHistory.getState(player.getServerWorld()).getDeathData(player.getUuid(), deathId);
                if (deathData != null)
                    for (DeadBodyData data : deathData.savedData()) {
                        data.transferTo(player);
                    }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(OPEN_DEAD_BODY_INV, (server, player, handler, buf, responseSender) -> {
            int deathId = buf.readInt();
            String invId = buf.readString();
            server.execute(() -> {
                DeathData deathData = DeathHistory.getState(player.getServerWorld()).getDeathData(player.getUuid(), deathId);
                if (deathData != null)
                    for (DeadBodyData data : deathData.savedData()) {
                        if (data.getId().equals(invId)) {
                            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                                @Override
                                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                                    buf.writeInt(deathId);
                                    buf.writeNbt(deathData.writeNbt());
                                }

                                @Override
                                public Text getDisplayName() {
                                    return Text.literal("Dead body");
                                }

                                @Nullable
                                @Override
                                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                                    return data.createMenu(syncId, playerInventory, player, deathData);
                                }
                            });
                        }

                    }
            });
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            ServerWorld world = server.getOverworld();
            DeathHistory data = DeathHistory.getState(world);
            DeathHistory purged = world.getPersistentStateManager()
                    .getOrCreate(nbt -> DeathHistory.readNbt(world, nbt), () -> new DeathHistory(world), "death_history_purged");
            int purgedCount = data.purgeOldEntries(purged);
            if (purgedCount > 0)
                purged.markDirty();
        });

        BodiesBodiesCommands.registerCommands();

        if (FabricLoader.getInstance().isModLoaded("trinkets"))
            TrinketCompat.load();
    }

    public static void createDeadBody(ServerPlayerEntity player) {
        DeadBodyEntity deadBodyEntity = DeadBodyEntity.create(player);
        player.getWorld().spawnEntity(deadBodyEntity);
    }
}
