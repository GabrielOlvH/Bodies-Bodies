package me.steven.bodiesbodies.entity;

import me.steven.bodiesbodies.BodiesBodies;
import me.steven.bodiesbodies.Config;
import me.steven.bodiesbodies.data.DeadBodyData;
import me.steven.bodiesbodies.data.DeadBodyDataProvider;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.data.persistentstate.DeathHistory;
import me.steven.bodiesbodies.data.VanillaDeadBodyData;
import me.steven.bodiesbodies.screen.VanillaDeadBodyInventoryScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DeadBodyEntity extends Entity {

    public static final TrackedData<NbtCompound> INVENTORY_DATA = DataTracker.registerData(DeadBodyEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    public static final TrackedData<Optional<UUID>> PLAYER_UUID = DataTracker.registerData(DeadBodyEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private int deathDataId;
    private int emptyTimer = 0;

    public DeadBodyEntity(EntityType<?> type, World world) {
        super(type, world);
        this.dataTracker.startTracking(PLAYER_UUID, Optional.empty());
        this.dataTracker.startTracking(INVENTORY_DATA, new NbtCompound());
        this.intersectionChecked = false;
        this.setPose(EntityPose.SLEEPING);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;

        ServerWorld world = (ServerWorld) getWorld();
        if (Config.CONFIG.nonEmptyBodyDisappearAfter > 0 && age > Config.CONFIG.nonEmptyBodyDisappearAfter) {
            discard();
        }
        DeathData deathData = getDeathData(world);

        if (deathData != null) {
            for (DeadBodyData data : deathData.savedData()) {
                if (!data.isEmpty()) return;
            }
        }

        emptyTimer++;
        if (Config.CONFIG.emptyBodyDisappearAfter > 0 && emptyTimer > Config.CONFIG.emptyBodyDisappearAfter) {
            discard();
        }
    }

    public DeathData getDeathData(ServerWorld world) {
        return DeathHistory.getState(world).getDeathData(getPlayerUUID(), deathDataId);
    }

    @Override
    public boolean canHit() {
        return true;
    }

    public static DeadBodyEntity create(ServerPlayerEntity player) {
        DeathHistory history = DeathHistory.getState(player.getServerWorld());
        int id = history.backup(player);
        System.out.println("Player " + player + " died. Death ID: " + id);

        DeadBodyEntity deadBody = new DeadBodyEntity(BodiesBodies.DEAD_BODY_ENTITY_TYPE, player.getWorld());
        deadBody.setPos(player.getX(), player.getY(), player.getZ());
        deadBody.dataTracker.set(PLAYER_UUID, Optional.of(player.getUuid()));
        List<DeadBodyData> savedData = new ArrayList<>(DeadBodyDataProvider.init(player));

        NbtCompound nbt = new NbtCompound();
        for (DeadBodyData data : savedData) {
            nbt.put(data.getId(), data.write(new NbtCompound()));
        }
        deadBody.dataTracker.set(INVENTORY_DATA, nbt);

        deadBody.resetPosition();
        deadBody.refreshPosition();


        deadBody.deathDataId = history.save(id, player,deadBody.getBlockPos(), savedData);

        return deadBody;
    }

    public UUID getPlayerUUID() {
        return dataTracker.get(PLAYER_UUID).get();
    }
    @Override
    protected Box calculateBoundingBox() {
        Box box = super.calculateBoundingBox();
        return new Box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ).shrink(0.0, 1.2, 0.0).expand(0.6, 0.0, 0.1);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.getWorld().isClient) return ActionResult.SUCCESS;

        if (Config.CONFIG.bodyAccessibleByAnyoneAfter > 0 && age < Config.CONFIG.bodyAccessibleByAnyoneAfter && dataTracker.get(PLAYER_UUID).isPresent() && !player.getUuid().equals(dataTracker.get(PLAYER_UUID).get())){
            player.sendMessage(Text.literal("This body does not belong to you!"));
            return ActionResult.PASS;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        DeathData deathData = getDeathData(world);

        if (player.isSneaking()) {
            for (DeadBodyData data : deathData.savedData()) {
                data.transferTo(player);
            }
        } else {
            for (DeadBodyData data : deathData.savedData()) {
                if (data instanceof VanillaDeadBodyData vanillaDeadBodyData) {
                    player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                        @Override
                        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                            buf.writeInt(deathData.id());
                            buf.writeNbt(deathData.writeNbt());
                        }

                        @Override
                        public Text getDisplayName() {
                            return Text.literal("Dead body");
                        }

                        @Override
                        public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                            return new VanillaDeadBodyInventoryScreenHandler(syncId, playerInventory, getDeathData(world), vanillaDeadBodyData);
                        }
                    });
                }
            }

        }
        return ActionResult.SUCCESS;
    }

    public Optional<UUID> getPlayerUuid() {
        return dataTracker.get(PLAYER_UUID);
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(PLAYER_UUID, Optional.of(nbt.getUuid("PlayerUUID")));
        this.deathDataId = nbt.getInt("DeathDataId");
        ServerWorld world = (ServerWorld) getWorld();
        DeathData deathData = getDeathData(world);
        if (deathData != null) {
            NbtCompound newNbt = new NbtCompound();
            for (DeadBodyData data : deathData.savedData()) {
                newNbt.put(data.getId(), data.write(new NbtCompound()));
            }
            this.dataTracker.set(INVENTORY_DATA, newNbt);
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("DeathDataId", deathDataId);
        nbt.putUuid("PlayerUUID", this.dataTracker.get(PLAYER_UUID).orElse(null));
    }
}
