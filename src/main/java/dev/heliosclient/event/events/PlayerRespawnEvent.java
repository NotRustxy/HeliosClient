package dev.heliosclient.event.events;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.entity.player.PlayerEntity;

@Cancelable

public class PlayerRespawnEvent extends Event {
    private final PlayerEntity player;

    public PlayerRespawnEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
