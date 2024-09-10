package me.djelectro.snipebot.types;

import me.djelectro.snipebot.Database;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

import javax.xml.crypto.Data;
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
}
