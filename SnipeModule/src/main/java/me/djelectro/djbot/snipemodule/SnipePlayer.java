package me.djelectro.djbot.snipemodule;

import me.djelectro.djbot.Database;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class SnipePlayer {

    private final Member discordMember;

    public SnipePlayer(Member discordMember){
        this.discordMember = discordMember;
    }

    public String getDiscordId(){
        return discordMember.getId();
    }

    public User getDiscordUser(){return discordMember.getUser();}

    public Member getDiscordMember(){return discordMember;}

    public int getSnipeCount(SnipeGuild g){
        Map<Integer, String[]> res = Database.getInstance().executeAndReturnData("SELECT COUNT(*) FROM snipes WHERE userid = ? AND guildid = ?", discordMember.getId(), g.getId());
        if(res.isEmpty()){
            return 0;
        }
        return Integer.parseInt(res.entrySet().iterator().next().getValue()[0]);
    }

    public int getSnipedCount(SnipeGuild g){
        Map<Integer, String[]> res = Database.getInstance().executeAndReturnData("SELECT COUNT(*) FROM snipes WHERE snipedid = ? AND guildid = ?", discordMember.getId(), g.getId());
        if(res.isEmpty()){
            return 0;
        }
        return Integer.parseInt(res.entrySet().iterator().next().getValue()[0]);
    }

    public static SnipePlayer getPlayerFromMapping(OptionMapping om){
        if(om == null) {
            return null;
        }
        try {
            return new SnipePlayer(om.getAsMember());
        }catch (IllegalStateException _){
            return null;
        }
    }

}
