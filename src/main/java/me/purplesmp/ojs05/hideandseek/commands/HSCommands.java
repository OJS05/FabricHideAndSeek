package me.purplesmp.ojs05.hideandseek.commands;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.purplesmp.ojs05.hideandseek.HideAndSeek;
import me.purplesmp.ojs05.hideandseek.objects.HSPlayer;
import me.purplesmp.ojs05.hideandseek.utilities.GameManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.literal;


public class HSCommands {

    public static void register(){

        final List<UUID> cooldown = new ArrayList<>();

        GameManager gameManager = HideAndSeek.getInstance().getGameManager();

        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> dispatcher.register(literal("hideandseek")
                .requires(Permissions.require("hideandseek.main"))
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(new LiteralText("help message"), MessageType.CHAT,context.getSource().getPlayer().getUuid());

                    return 1;
                })
                .then(literal("create")
                    .requires(Permissions.require("hideandseek.admin"))
                        .executes(context -> {
                            try{
                                gameManager.createGame(context.getSource());

                                return 1;
                            }catch(StackOverflowError e){
                                System.out.println(e);
                                context.getSource().getServer().getPlayerManager().getPlayer("OJS05").sendMessage(new LiteralText(e.toString()),MessageType.SYSTEM,null);
                                return 0;
                            }
                        })
                )
                .then(literal("cancel")
                    .requires(Permissions.require("hideandseek.admin"))
                        .executes(context -> {
                            gameManager.finishGame();

                            return 1;
                        })
                )
                .then(literal("hint")
                    .requires(Permissions.require("hideandseek.main"))
                        .executes(context -> {
                            if(gameManager.isGameRunning()){
                                if(context.getSource().getPlayer().isPlayer()){
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    HSPlayer hsPlayer = HSPlayer.getExact(player.getUuid());
                                    if(gameManager.getSeekers().getMembers().contains(hsPlayer)){
                                        if(!cooldown.contains(hsPlayer.getUuid())){
                                            int randomIndex = GameManager.getRandom().nextInt(gameManager.getHiders().getMembers().size());
                                            HSPlayer randomHSPlayerHider = gameManager.getHiders().getMembers().get(randomIndex);
                                            ServerPlayerEntity randomHider = context.getSource().getServer().getPlayerManager().getPlayer(randomHSPlayerHider.getUuid());

                                            if(randomHider != null){
                                                int approximateX = GameManager.getRandom().nextInt(randomHider.getBlockX() - 15, randomHider.getBlockX() + 15);
                                                int approximateY = GameManager.getRandom().nextInt(randomHider.getBlockY() - 15, randomHider.getBlockY() + 15);
                                                int approximateZ = GameManager.getRandom().nextInt(randomHider.getBlockZ() - 15, randomHider.getBlockZ() + 15);

                                                player.sendMessage(new LiteralText(Formatting.AQUA + "There is a random hider around " + Formatting.GOLD + approximateX + ", " + approximateY + ", " + approximateZ),MessageType.CHAT,player.getUuid());
                                            }

                                            cooldown.add(player.getUuid());

                                            HideAndSeek.getScheduler().schedule(() -> {
                                                cooldown.remove(player.getUuid());
                                            }, (long) (gameManager.getSeekers().getMembers().size() * 0.75), TimeUnit.MINUTES);
                                        }else{
                                            player.sendMessage(new LiteralText(Formatting.RED + "You are still on a cooldown."), MessageType.CHAT, player.getUuid());
                                        }
                                    }
                                    if(gameManager.getHiders().getMembers().contains(hsPlayer)){
                                        if (!cooldown.contains(player.getUuid())) {
                                            double distMin = 1000000000;
                                            for (HSPlayer seekers : gameManager.getSeekers().getMembers()) {
                                                ServerPlayerEntity seekerPlayer = context.getSource().getServer().getPlayerManager().getPlayer(seekers.getUuid());

                                                if (seekerPlayer != null) {

                                                    double dist = player.getPos().distanceTo(seekerPlayer.getPos());

                                                    if (dist < distMin) {
                                                        distMin = dist;
                                                    }


                                                }
                                            }

                                            player.sendMessage(new LiteralText(Formatting.AQUA + "The nearest seeker is " + Formatting.DARK_RED + Math.round(distMin) + Formatting.AQUA + " blocks away."), MessageType.CHAT, player.getUuid());

                                            cooldown.add(player.getUuid());

                                            HideAndSeek.getScheduler().schedule(() -> {
                                                cooldown.remove(player.getUuid());
                                            }, (long) (gameManager.getHiders().getMembers().size() * 0.5),TimeUnit.MINUTES);
                                        } else {
                                            player.sendMessage(new LiteralText(Formatting.RED + "You are still on a cooldown"), MessageType.CHAT, player.getUuid());
                                        }
                                    }
                                }
                            }


                            return 1;
                        })
                )
                .then(literal("list")
                    .requires(Permissions.require("hideandseek.main"))
                        .executes(context -> {
                            if (gameManager.isGameRunning()) {

                                ServerPlayerEntity player = context.getSource().getPlayer();

                                player.sendMessage(new LiteralText(Formatting.DARK_RED + "Seekers:"), MessageType.CHAT, player.getUuid());
                                gameManager.getSeekers().getMembers().forEach(hsPlayer ->
                                        player.sendMessage(new LiteralText(Formatting.RED + "- " + hsPlayer.getName()),MessageType.CHAT, player.getUuid()));

                                player.sendMessage(new LiteralText(Formatting.GOLD + "Hiders:"), MessageType.CHAT, player.getUuid());
                                gameManager.getHiders().getMembers().forEach(hsPlayer ->
                                        player.sendMessage(new LiteralText(Formatting.GOLD + "- " + hsPlayer.getName()),MessageType.CHAT, player.getUuid()));
                            }

                            return 1;
                        })
                )
        )));
    }
}
