package me.djelectro.djbot.modules;

import me.djelectro.djbot.annotations.SlashCommand;
import me.djelectro.djbot.annotations.SlashCommandOption;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class PingPong extends Module {

    @SlashCommand(name = "ping", description = "Hello World")
    public void pingPong(SlashCommandInteractionEvent event){
        event.reply("Pong!").queue();
    }

    @SlashCommand(name="echo", description = "I echo you!", options = {
            @SlashCommandOption(name="msg", option= OptionType.STRING, description = "Your message", required = true)
    })
    public void echo(SlashCommandInteractionEvent event){
        event.reply(event.getOption("msg").getAsString()).queue();
    }
}
