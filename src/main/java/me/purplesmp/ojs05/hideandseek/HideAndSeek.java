package me.purplesmp.ojs05.hideandseek;

import lombok.Getter;
import me.purplesmp.ojs05.hideandseek.commands.HSCommands;
import me.purplesmp.ojs05.hideandseek.mixin.PlayerJoinCallback;
import me.purplesmp.ojs05.hideandseek.mixin.PlayerLeaveCallback;
import me.purplesmp.ojs05.hideandseek.objects.HSPlayer;
import me.purplesmp.ojs05.hideandseek.utilities.GameManager;
import me.purplesmp.ojs05.hideandseek.utilities.TeamType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.ActionResult;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HideAndSeek implements ModInitializer {

    private static HideAndSeek instance;

    public static HideAndSeek getInstance(){
        return instance;
    }

    private GameManager gameManager;

    public GameManager getGameManager(){
        return gameManager;
    }

    private static MinecraftServer server;

    public static MinecraftServer getServer(){
        return server;
    }

    private final static ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    public static ScheduledThreadPoolExecutor getScheduler(){
        return scheduler;
    }

    @Override
    public void onInitialize(){
        instance = this;
        this.server = getServer();

        gameManager = new GameManager();
        gameManager.setupGame();

        HSCommands.register();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if(gameManager.isGameRunning()){
                Entity damagingEntity = player;
                Entity victimEntity = entity;

                if(damagingEntity instanceof PlayerEntity && victimEntity instanceof PlayerEntity){
                    PlayerEntity attacker = player;
                    PlayerEntity victim = (PlayerEntity) entity;

                    HSPlayer victimHsPlayer = HSPlayer.getExact(victim.getUuid());
                    HSPlayer damagerHsPlayer = HSPlayer.getExact(attacker.getUuid());

                    if (damagerHsPlayer.getCurrentTeam().getTeamType() == TeamType.SEEKER &&
                            victimHsPlayer.getCurrentTeam().getTeamType() == TeamType.HIDER) {
                        victimHsPlayer.setCurrentTeam(damagerHsPlayer.getCurrentTeam(), true);
                    }

                }
            }
            return ActionResult.SUCCESS;
        });

        PlayerLeaveCallback.EVENT.register((playerEntity -> {
            if(gameManager.isGameRunning()){
                HSPlayer player = HSPlayer.getExact(playerEntity.getUuid());
                if(player.getCurrentTeam().getTeamType() == TeamType.HIDER){
                    player.startLeaveTask();
                }
            }
        }));

        PlayerJoinCallback.EVENT.register((playerEntity -> {
            if (gameManager.isGameRunning()){
                HSPlayer player = HSPlayer.getExact(playerEntity.getUuid());
                
                player.cancelLeaveTask();
            }
        }));
    }

}
