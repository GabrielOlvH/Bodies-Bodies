package me.steven.bodiesbodies.data;

import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public interface DeadBodyDataProvider {
    List<DeadBodyDataProvider> PROVIDERS = new ArrayList<>();

    static void register(DeadBodyDataProvider provider) {
        PROVIDERS.add(provider);
    }

    static List<DeadBodyData> init(PlayerEntity player) {
        List<DeadBodyData> data = new ArrayList<>();
        for (DeadBodyDataProvider provider : PROVIDERS) {
            data.add(provider.create().transferFrom(player));
        }
        return data;
    }

    static List<DeadBodyData> initEmpty() {
        List<DeadBodyData> data = new ArrayList<>();
        for (DeadBodyDataProvider provider : PROVIDERS) {
            data.add(provider.create());
        }
        return data;
    }
    DeadBodyData create();
}
