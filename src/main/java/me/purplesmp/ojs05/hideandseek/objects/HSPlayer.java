package me.purplesmp.ojs05.hideandseek.objects;

import lombok.Getter;
import me.purplesmp.ojs05.hideandseek.HideAndSeek;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HSPlayer {

    private static final ConcurrentHashMap<UUID, HSPlayer> hsPlayerMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<UUID, HSPlayer> getHsPlayerMap(){
        return hsPlayerMap;
    }

    private final UUID uuid;

    public UUID getUuid(){
        return uuid;
    }

    private final String name;

    public String getName(){
        return name;
    }

    private HSTeam currentTeam;

    public HSTeam getCurrentTeam(){
        return currentTeam;
    }

    private ScheduledFuture leaveTask;

    public HSPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        hsPlayerMap.put(uuid, this);
    }

    public void setCurrentTeam(HSTeam newTeam, boolean triggerUpdate) {
        if (currentTeam != null) currentTeam.removePlayer(this);

        if (newTeam == null) {
            this.currentTeam = null;
            return;
        }

        newTeam.addPlayer(this);

        this.currentTeam = newTeam;

        HideAndSeek.getServer().getPlayerManager().broadcastChatMessage(new LiteralText(Formatting.GOLD + name + Formatting.AQUA + " is now a " + Formatting.GOLD + newTeam.getName()), MessageType.CHAT, this.getUuid());

        if (triggerUpdate) {
            // Calculate winner after every team change
            HideAndSeek.getInstance().getGameManager().calculateWinner();
        }
    }

    public static HSPlayer getExact(UUID uuid) {
        return hsPlayerMap.get(uuid);
    }

    public void startLeaveTask() {
        leaveTask = HideAndSeek.getScheduler().schedule(() -> setCurrentTeam(HideAndSeek.getInstance().getGameManager().getSeekers(),true),1, TimeUnit.MINUTES);
    }

    public void cancelLeaveTask() {
        if (!leaveTask.isCancelled()) {
            leaveTask.cancel(true);
        }
    }


}
