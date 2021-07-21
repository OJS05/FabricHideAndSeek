package me.purplesmp.ojs05.hideandseek.objects;

import me.purplesmp.ojs05.hideandseek.utilities.TeamType;

import java.util.ArrayList;
import java.util.List;

public class HSTeam {

    private final String name;

    public String getName() {
        return name;
    }

    private final TeamType teamType;

    public TeamType getTeamType() {
        return teamType;
    }

    private final List<HSPlayer> members = new ArrayList<>();

    public List<HSPlayer> getMembers() {
        return members;
    }

    public HSTeam(String name, TeamType teamType) {
        this.name = name;
        this.teamType = teamType;
    }

    public void addPlayer(HSPlayer hsPlayer) {
        members.add(hsPlayer);
    }

    public void removePlayer(HSPlayer hsPlayer) {
        members.remove(hsPlayer);
    }

}
