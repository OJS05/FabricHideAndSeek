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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public static ThreadLocalRandom getRandom() {
        return random;
    }

    private static HSTeam hiders;

    public static HSTeam getHiders(){
        return hiders;
    }

    private static HSTeam seekers;

    public static HSTeam getSeekers(){
        return seekers;
    }

    @Getter
    @Setter
    private static boolean canHiderJoin;

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

        canHiderJoin = true;
        gameRunning = true;

        taskList.add(HideAndSeek.getScheduler().schedule(() -> {
            canHiderJoin = false;
        }, 1, TimeUnit.SECONDS));

        taskList.add(HideAndSeek.getScheduler().schedule(() -> {
            gameRunning = false;
        }, 1, TimeUnit.SECONDS));

        taskList.add(HideAndSeek.getScheduler().schedule(this::calculateWinner, 1, TimeUnit.SECONDS));
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

}
