package me.purplesmp.ojs05.hideandseek.mixin;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerJoinCallback {
    Event<PlayerJoinCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinCallback.class, callbacks -> playerEntity -> {
        for (PlayerJoinCallback callback : callbacks) {
            callback.onJoin(playerEntity);
        }
    });

    void onJoin(ServerPlayerEntity playerEntity);
}
