package me.purplesmp.ojs05.hideandseek.utilities;

import de.leonhard.storage.Json;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class Config {
    private static Json json = new Json("hideandseek", FabricLoader.getInstance().getConfigDir().toString());

    private static List<String> exemptPlayerList = json.getStringList("exempt-players");

    public static List<String> getExemptPlayerList(){
        return exemptPlayerList;
    }
}
