package me.purplesmp.ojs05.hideandseek;

import me.purplesmp.ojs05.hideandseek.commands.HSCommands;
import me.purplesmp.ojs05.hideandseek.objects.HSPlayer;
import me.purplesmp.ojs05.hideandseek.utilities.GameManager;
import me.purplesmp.ojs05.hideandseek.utilities.TeamType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class HideAndSeek implements ModInitializer {

    private static HideAndSeek instance;

    public static HideAndSeek getInstance() {
        return instance;
    }

    private GameManager gameManager;

    public GameManager getGameManager() {
        return gameManager;
    }

    private static MinecraftServer server;

    public static MinecraftServer getServer() {
        return server;
    }

    private final static ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    public static ScheduledThreadPoolExecutor getScheduler() {
        return scheduler;
    }

    @Override
    public void onInitialize() {
        instance = this;
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);


        gameManager = new GameManager();
        gameManager.setupGame();

        HSCommands.register();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (gameManager.isGameRunning()) {

                if (player != null && entity instanceof PlayerEntity victim) {

                    HSPlayer victimHsPlayer = HSPlayer.getExact(victim.getUuid());
                    HSPlayer damagerHsPlayer = HSPlayer.getExact(player.getUuid());

                    if (damagerHsPlayer.getCurrentTeam().getTeamType() == TeamType.SEEKER &&
                            victimHsPlayer.getCurrentTeam().getTeamType() == TeamType.HIDER) {
                        victimHsPlayer.setCurrentTeam(damagerHsPlayer.getCurrentTeam(), true);
                    }

                }
            }
            return ActionResult.SUCCESS;
        });

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server1) -> {

            ServerPlayerEntity player = handler.getPlayer();

            HSPlayer hsPlayer = HSPlayer.getOrCreate(player.getUuid(), player.getName().getString());

            if (gameManager.isGameRunning()) {
                if (hsPlayer.getCurrentTeam() == null) {
                    if (gameManager.isCanHidersJoin()) {
                        hsPlayer.setCurrentTeam(HideAndSeek.getInstance().getGameManager().getHiders(), true);
                    }
                }

                hsPlayer.cancelLeaveTask();
            }
        }));

        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server1) -> {
            if (gameManager.isGameRunning()) {
                HSPlayer player = HSPlayer.getExact(handler.getPlayer().getUuid());
                if (player.getCurrentTeam().getTeamType() == TeamType.HIDER) {
                    player.startLeaveTask();
                }
            }
        }));
    }

    private void onServerStarted(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

}
