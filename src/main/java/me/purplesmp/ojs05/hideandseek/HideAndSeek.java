package me.purplesmp.ojs05.hideandseek;

import lombok.Getter;
import me.purplesmp.ojs05.hideandseek.utilities.GameManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

public class HideAndSeek implements ModInitializer {

    @Getter
    private static HideAndSeek instance;

    @Getter
    private GameManager gameManager;

    @Getter
    private static MinecraftServer server;

    @Override
    public void onInitialize(){
        instance = this;
        this.server = getServer();

        gameManager = new GameManager();
        gameManager.setupGame();
    }


}
