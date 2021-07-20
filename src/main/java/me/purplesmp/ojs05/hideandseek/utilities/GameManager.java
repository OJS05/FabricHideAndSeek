package me.purplesmp.ojs05.hideandseek.utilities;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.purplesmp.ojs05.hideandseek.HideAndSeek;
import me.purplesmp.ojs05.hideandseek.objects.HSPlayer;
import me.purplesmp.ojs05.hideandseek.objects.HSTeam;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public static ThreadLocalRandom getRandom() {
        return random;
    }

    private HSTeam hiders;

    public HSTeam getHiders(){
        return hiders;
    }

    private HSTeam seekers;

    public HSTeam getSeekers(){
        return seekers;
    }

    private boolean gameRunning;

    public boolean isGameRunning(){
        return gameRunning;
    }

    @Getter
    private int gameLength;

    @Getter
    private final List<ScheduledFuture> taskList = new ArrayList<>();




    public void setupGame() {
        this.hiders = new HSTeam("Hider", TeamType.HIDER);
        this.seekers = new HSTeam("Seeker", TeamType.SEEKER);

        gameLength = 15;
    }

    public void createGame() {
        if (gameRunning) return;
        ImmutableList<ServerPlayerEntity> onlinePlayers = ImmutableList.copyOf(HideAndSeek.getServer().getPlayerManager().getPlayerList());
        ServerPlayerEntity randomPlayer = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
        HSPlayer randomHsPlayer = HSPlayer.getExact(randomPlayer.getUuid());

        randomHsPlayer.setCurrentTeam(seekers, false);

        onlinePlayers.forEach(player -> {
            HSPlayer hsPlayer = HSPlayer.getExact(player.getUuid());
            if (hsPlayer.getCurrentTeam() == null) hsPlayer.setCurrentTeam(hiders, false);
        });

        gameRunning = true;

        taskList.add(HideAndSeek.getScheduler().schedule(this::finishGame, gameLength, TimeUnit.MINUTES));

        taskList.add(HideAndSeek.getScheduler().schedule(this::calculateWinner, 899, TimeUnit.SECONDS));
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

        taskList.forEach(scheduledFuture -> {
            if (!scheduledFuture.isCancelled()) scheduledFuture.cancel(true);
        });

    }

}
