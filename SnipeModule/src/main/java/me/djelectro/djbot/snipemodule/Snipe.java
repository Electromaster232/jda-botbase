package me.djelectro.djbot.snipemodule;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.djelectro.djbot.annotations.SlashCommand;
import me.djelectro.djbot.annotations.SlashCommandOption;
import me.djelectro.djbot.modules.Module;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class Snipe extends Module {

    @SlashCommand(name = "snipe", description = "Snipe a player", perms = Permission.MESSAGE_SEND, options = {
            @SlashCommandOption(name="member", description = "The player you sniped", required = true, option = OptionType.MENTIONABLE),
            @SlashCommandOption(name = "attachment", option = OptionType.ATTACHMENT, description = "Attach your image", required = true),
            @SlashCommandOption(name="message", option = OptionType.STRING, description = "An optional message", required = false)
    })
    public void snipeCmd(SlashCommandInteractionEvent event) throws Exception {
        event.deferReply().queue();
        SnipeImpl s = SnipeImpl.createSnipe(event);
        if(s == null) {
            event.getHook().sendMessage("(Error code 3) Snipe failed to process. Did you specify a valid user in your mention?").queue();
            return;
        }
        if(!s.process()){
            event.getHook().sendMessage("Snipe failed to process, code 1").queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();
        if(guildId == 0){
            event.getHook().sendMessage("Snipe failed to process, code 2").queue();
            return;
        }

        try(SnipeGuild g = s.getGuild()){
            // Make sure that this guild is valid in DB

            WebhookClient client = WebhookClient.withUrl(g.getSnipeHook().getUrl());
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            String msg = s.getMessage();
            if(msg == null)
                msg = "";
            builder.setContent(STR."\{s.getSniped().getDiscordMember().getAsMention()}\n\{msg}\n\{s.getAttachUrl()}");
            builder.setUsername(event.getMember().getEffectiveName());
            builder.setAvatarUrl(event.getMember().getEffectiveAvatarUrl());
            client.send(builder.build()).join();
        };
        event.getHook().sendMessage("Snipe recorded.").queue();
//        String channelString = Database.getInstance().executeAndReturnData("SELECT channelid FROM guild_config WHERE guildid = ?", String.valueOf(guildId)).entrySet().iterator().next().getValue()[0];
//        long channelID = Long.parseLong(channelString);
//
//        event.getGuild().getTextChannelById(channelID).sendMessage(s.getSniper().getDiscordMember().getEffectiveName() + " sniped " + s.getSniped().getDiscordMember().getEffectiveName()).queue();
    }

    @SlashCommand(name = "getsnipes", description = "Show how many snipes a player has", options = {
            @SlashCommandOption(name="user", option = OptionType.MENTIONABLE, description = "The user to lookup", required = true)
    })
    public void showSnipes(SlashCommandInteractionEvent e){
        e.deferReply().queue();
        OptionMapping om = e.getOption("user");
        if(om == null) {
            e.reply("Error locating user").queue();
            return;
        }
        Member m = om.getAsMember();
        User u = om.getAsUser();
        if(m == null){
            e.getHook().sendMessage("Could not process your request. Did you specify a valid user?").queue();
            return;
        }
        SnipePlayer sp = new SnipePlayer(u);
        e.getHook().sendMessage(STR."\{m.getEffectiveName()} has sniped \{sp.getSnipeCount(new SnipeGuild(e.getGuild()))} players in this guild.\nThey have been sniped \{sp.getSnipedCount(new SnipeGuild(e.getGuild()))} times.").queue();
    }

    @SlashCommand(name="snipeconfig", description = "Set the configuration for this guild", options = {
            @SlashCommandOption(name="snipechannel", option=OptionType.CHANNEL, description = "The channel to send snipes to", required = true)
    })
    public void guildConfig(SlashCommandInteractionEvent e){
        // Need to check if this guild exists in the DB
        e.deferReply().queue();
        SnipeGuild sg = new SnipeGuild(e.getGuild());
        OptionMapping c = e.getOption("snipechannel");
        if(c == null){
            e.getHook().sendMessage("Cannot locate channel").queue();
            return;
        }
        try {
            TextChannel tc = c.getAsChannel().asTextChannel();
            if(sg.updateSnipeChannel(tc))
                e.getHook().sendMessage("Guild configuration saved.").queue();
            else
                e.getHook().sendMessage("There was an error updating the guild config").queue();
        }catch (IllegalStateException ise){
            e.getHook().sendMessage("The channel specified is not a text channel!").queue();
        }
    }
}
