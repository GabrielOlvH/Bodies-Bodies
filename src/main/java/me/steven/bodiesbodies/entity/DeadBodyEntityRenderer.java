package me.steven.bodiesbodies.entity;

import com.mojang.authlib.GameProfile;
import me.steven.bodiesbodies.Config;
import me.steven.bodiesbodies.data.DeadBodyData;
import me.steven.bodiesbodies.data.DeadBodyDataProvider;
import me.steven.bodiesbodies.data.persistentstate.DeathData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DeadBodyEntityRenderer extends EntityRenderer<DeadBodyEntity> {
    public static SkeletonEntity fakeSkeleton = null;


    public static SkeletonEntity getFakeSkeleton() {
        if (fakeSkeleton == null) {
            fakeSkeleton = new SkeletonEntity(EntityType.SKELETON, MinecraftClient.getInstance().world);
            fakeSkeleton.setPose(EntityPose.SLEEPING);
            fakeSkeleton.prevHeadYaw = 25;
            fakeSkeleton.setHeadYaw(25);
        }
        return fakeSkeleton;
    }

    public DeadBodyEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(DeadBodyEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        MinecraftClient client = MinecraftClient.getInstance();
        matrices.push();
        matrices.translate(1.0, 0.0, 0.0);
        List<DeadBodyData> savedData = new ArrayList<>(DeadBodyDataProvider.initEmpty());
        savedData.addAll(DeadBodyDataProvider.initEmpty());
        for (DeadBodyData data : savedData) {
            data.read(entity.getDataTracker().get(DeadBodyEntity.INVENTORY_DATA).getCompound(data.getId()));
        }
        Optional<UUID> playerUuid = entity.getPlayerUuid();
        if (playerUuid.isPresent() && entity.age < Config.CONFIG.bodyTurnSkeletonTime) {
            UUID uuid = playerUuid.get();
            ClientPlayerEntity copyPlayer = new ClientPlayerEntity(client, client.world, new ClientPlayNetworkHandler(null, null, new ClientConnection(NetworkSide.CLIENTBOUND), null, new GameProfile(uuid, "null"), null), null, null, false, false) {
                @Override
                public boolean shouldRenderName() {
                    return false;
                }
            };

            copyPlayer.setPose(EntityPose.SLEEPING);
            copyPlayer.prevHeadYaw = 25;
            copyPlayer.setHeadYaw(25);
            for (DeadBodyData data : savedData) {
                data.transferTo(copyPlayer);
            }
            copyPlayer.setUuid(uuid);

            client.getEntityRenderDispatcher().getRenderer(copyPlayer).render(copyPlayer, 0f, tickDelta, matrices, vertexConsumers, light);
            matrices.pop();
            return;

        }

        SkeletonEntity fakeSkeleton = getFakeSkeleton();
        for (DeadBodyData data :  savedData) {
            data.transferTo(fakeSkeleton);
        }

        client.getEntityRenderDispatcher().getRenderer(fakeSkeleton).render(fakeSkeleton, 0f, tickDelta, matrices, vertexConsumers, light);
        matrices.pop();

    }

    @Override
    public Identifier getTexture(DeadBodyEntity entity) {
        return null;
    }
}
