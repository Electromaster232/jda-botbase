package me.djelectro.djbot.snipemodule;

import me.djelectro.djbot.Database;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SnipeGuild implements AutoCloseable {

    private Webhook hook;
    private Guild guild;

    private TextChannel snipeChannel;

    public SnipeGuild(Guild i){
        this.guild = i;
    }

    public String getId(){return guild.getId();}

    public TextChannel getSnipeChannel(){
        if(snipeChannel != null)
            return snipeChannel;
        // Figure out channel ID
        Map<Integer, String[]> channelData = Database.getInstance().executeAndReturnData("SELECT channelid FROM guild_config WHERE guildid = ?", String.valueOf(guild.getId()));
        if(channelData.isEmpty())
            return null;
        String channelString = channelData.entrySet().iterator().next().getValue()[0];
        return guild.getTextChannelById(channelString);
    }

    public boolean updateSnipeChannel(TextChannel newChannelId){
        boolean result;
        if(getSnipeChannel() != null){
            // This guild exists, do an update
            result =  Database.getInstance().executeUpdate("UPDATE guild_config SET channelid = ? WHERE guildid = ?", newChannelId.getId(), guild.getId());
        }
        else
            result =  Database.getInstance().executeUpdate("INSERT INTO guild_config (guildid, channelid) VALUES(?, ?)", guild.getId(), newChannelId.getId());
        this.snipeChannel = newChannelId;
        return result;
    }

    public Webhook getSnipeHook(){
        if(this.hook != null)
            return this.hook;
        Webhook h = getSnipeChannel().createWebhook("snipe").complete();
        this.hook = h;
        return h;
    }

    @Override
    public void close() throws Exception {
        // IF we have a Webhook open from getting a snipehook, destroy it
        if(this.hook != null)
            hook.delete().queue();
    }

    /**
     * Get all players who have participated in this Guild
     * @return An array of Players for this guild
     * WARNING: WILL BLOCK THREAD UNTIL IT CAN RETRIEVE ALL MEMBERS FROM DISCORD
     */
    public SnipePlayer[] getGuildPlayers(){
        Map<Integer, String[]> results = Database.getInstance().executeAndReturnData("SELECT DISTINCT userid FROM snipes WHERE guildid = ? ;", getId());
        // Each individual player has their own String[] in the SQL call... so we need to merge all of these arrays while also converting to SnipePlayer
        // This has to be a dynamic list because it is not guaranteed that the SQL call will return 10 valid users (i.e. user who left the guild should not be counted and will return null)
        List<SnipePlayer> playerIds = new ArrayList<>();
        results.forEach((_, v) -> {
            try{
                Member m = guild.retrieveMemberById(v[0]).complete();
                if(m != null){
                    playerIds.add(new SnipePlayer(m));
                }
            }catch (ErrorResponseException _){}

        });
        return playerIds.toArray(new SnipePlayer[0]);

    }
}
