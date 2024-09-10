package me.djelectro.djbot.snipemodule;

import me.djelectro.djbot.Database;
import net.dv8tion.jda.api.entities.User;

import java.util.Map;

public class SnipePlayer {

    private User discordMember;

    public SnipePlayer(User discordMember){
        this.discordMember = discordMember;
    }

    public String getDiscordId(){
        return discordMember.getId();
    }

    public User getDiscordMember(){return discordMember;}

    public int getSnipeCount(SnipeGuild g){
        Map<Integer, String[]> res = Database.getInstance().executeAndReturnData("SELECT COUNT(*) FROM snipes WHERE userid = ? AND guildid = ?", discordMember.getId(), g.getId());
        if(res.isEmpty()){
            return 0;
        }
        return Integer.parseInt(res.entrySet().iterator().next().getValue()[0]);
    }

}
