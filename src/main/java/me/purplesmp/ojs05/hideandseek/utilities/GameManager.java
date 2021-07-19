package me.purplesmp.ojs05.hideandseek.utilities;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import me.purplesmp.ojs05.hideandseek.HideAndSeek;
import me.purplesmp.ojs05.hideandseek.objects.HSPlayer;
import me.purplesmp.ojs05.hideandseek.objects.HSTeam;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
    private final List<ScheduledFuture> taskList = new ArrayList<>();

    @Getter
    private final List<UUID> exemptPlayers = new ArrayList<>();

    @Getter
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    public void setupGame() {
        this.hiders = new HSTeam("Hider", TeamType.HIDER);
        this.seekers = new HSTeam("Seeker", TeamType.SEEKER);

        gameLength = HideAndSeek.getInstance().getConfig().getInt("game-length");

        // Load exempt users
        Config.load()
        Config.getConfig().getStringList("exempt-players").forEach(uuidString -> {
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

        taskList.add(scheduler.schedule(() -> {
            canHiderJoin = false;
        }, 1, TimeUnit.SECONDS));

        taskList.add(scheduler.schedule(() -> {
            gameRunning = false;
        }, 1, TimeUnit.SECONDS));

        taskList.add(scheduler.schedule(this::calculateWinner, 1, TimeUnit.SECONDS));
    }

    public void calculateWinner() {
        int hiderSize = hiders.getMembers().size();

        if (hiderSize == 0) {
            HideAndSeek.getServer().getPlayerManager().broadcastChatMessage(new LiteralText(Formatting.DARK_RED + "The seekers have won!"), MessageType.CHAT, null);
            finishGame();
        }
    }

    public void finishGame() {
        hiders.getMembers().clear();
        seekers.getMembers().clear();

        HSPlayer.getHsPlayerMap().values().forEach(hsPlayer -> hsPlayer.setCurrentTeam(null, false));

        this.gameRunning = false;
        this.canHiderJoin = false;

        taskList.forEach(scheduledFuture -> {
            if (!scheduledFuture.isCancelled()) scheduledFuture.cancel(true);
        });

    }

    private void saveExemptList() {
        List<String> uuidStringList = new ArrayList<>();
        exemptPlayers.forEach(uuid -> uuidStringList.add(uuid.toString()));

        HideAndSeek.getInstance().getConfig().set("exempt-players", uuidStringList);
        HideAndSeek.getInstance().saveConfig();
    }
}
