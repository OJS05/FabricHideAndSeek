package me.purplesmp.ojs05.hideandseek.utilities;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import me.purplesmp.ojs05.hideandseek.HideAndSeek;
import me.purplesmp.ojs05.hideandseek.objects.HSPlayer;
import me.purplesmp.ojs05.hideandseek.objects.HSTeam;
import net.minecraft.client.RunArgs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager {

    @Getter
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Getter
    private HSTeam hiders;

    @Getter
    private HSTeam seekers;

    @Getter
    @Setter
    private boolean canHiderJoin;

    @Getter
    private boolean gameRunning;

    @Getter
    private int gameLength;

    @Getter
    private final List<UUID> exemptPlayers = new ArrayList<>();

    public void setupGame() {
        this.hiders = new HSTeam("Hider", TeamType.HIDER);
        this.seekers = new HSTeam("Seeker", TeamType.SEEKER);

        gameLength = HideAndSeek.getInstance().getConfig().getInt("game-length");

        // Load exempt users
        HideAndSeek.getInstance().getConfig().getStringList("exempt-players").forEach(uuidString -> {
            UUID uuid = UUID.fromString(uuidString);
            exemptPlayers.add(uuid);
        });
    }

    public void createGame() {
        if (gameRunning) return;
        ImmutableList<ServerPlayerEntity> onlinePlayers = ImmutableList.copyOf(HideAndSeek.getServer().getPlayerManager().getPlayerList());
        ServerPlayerEntity randomPlayer = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
        HSPlayer randomHsPlayer = HSPlayer.getExact(randomPlayer.getUuid());

        randomHsPlayer.setCurrentTeam(seekers, false);

        onlinePlayers.forEach(player -> {
            HSPlayer hsPlayer = HSPlayer.getExact(player.getUuid());
            if (hsPlayer.getCurrentTeam() == null && !hsPlayer.isExempt()) hsPlayer.setCurrentTeam(hiders, false);
        });

        canHiderJoin = true;
        gameRunning = true;

    }

}
