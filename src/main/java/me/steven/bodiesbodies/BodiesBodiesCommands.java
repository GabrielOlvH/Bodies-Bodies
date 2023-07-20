package me.steven.bodiesbodies;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import me.steven.bodiesbodies.data.persistentstate.DeathHistory;
import me.steven.bodiesbodies.data.persistentstate.PlayerBackup;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BodiesBodiesCommands {
    public static void registerCommands() {
        registerDeathListCommand();
    }
    public static void registerDeathListCommand() {

        CommandRegistrationCallback.EVENT.register((commandDispatcher, b, env) ->
                commandDispatcher.register(CommandManager.literal("deathhistory")
                        .requires(s -> s.hasPermissionLevel(2))
                        .executes((ctx) -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            showDeathData(ctx, player.getUuid(), ctx.getSource().getWorld());
                            return 1;
                        }).then(CommandManager.argument("player", EntityArgumentType.player()).executes((ctx) -> {
                            PlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                            showDeathData(ctx, player.getUuid(), ctx.getSource().getWorld());
                            return 1;
                        }))));

        CommandRegistrationCallback.EVENT.register((commandDispatcher, b, env) ->
                commandDispatcher.register(CommandManager.literal("restorebackup")
                        .requires(s -> s.hasPermissionLevel(2))
                        .then(CommandManager.argument("deathId", IntegerArgumentType.integer()).executes((ctx) -> {
                            restoreNbtBackup(ctx, IntegerArgumentType.getInteger(ctx, "deathId"), ctx.getSource().getWorld());
                            return 1;
                        }))));
    }

    private static void showDeathData(CommandContext<ServerCommandSource> ctx, UUID uuid, ServerWorld world) {
        List<DeathData> deathsFor = DeathHistory.getState(world).getDeathsFor(uuid);
        if (deathsFor == null || deathsFor.isEmpty()) {
            ctx.getSource().sendMessage(Text.literal("No deaths for that player."));
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeInt(deathsFor.size());
        List<DeathData> list = deathsFor.stream().sorted(Collections.reverseOrder(Comparator.comparingInt(DeathData::id))).limit(8).toList();
        for (int i = 0; i < list.size(); i++) {
            DeathData deathData = list.get(i);
            buf.writeInt(i);
            buf.writeNbt(deathData.writeNbt());
        }

        ServerPlayNetworking.send(ctx.getSource().getPlayer(), BodiesBodies.OPEN_DEATH_HISTORY, buf);
    }

    private static void restoreNbtBackup(CommandContext<ServerCommandSource> ctx, int id, ServerWorld world) {
        PlayerBackup backup = DeathHistory.getState(world).getPlayerNbtBackup().get(id);
        if (backup == null) {
            ctx.getSource().sendMessage(Text.literal("No backup found for death id " + id));
            return;
        }

        NbtCompound nbt = backup.data();
        UUID uuid = nbt.getUuid("UUID");
        ServerPlayerEntity player = ctx.getSource().getServer().getPlayerManager().getPlayer(uuid);
        if (player == null) {
            ctx.getSource().sendMessage(Text.literal("No player found for UUID " + uuid));
            return;
        }

        nbt.remove("Health");
        player.readNbt(nbt);
    }
}
