package me.djelectro.snipebot.commands;

import me.djelectro.snipebot.annotations.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingPong extends Command{

    @SlashCommand(name = "ping", description = "Hello World")
    public void pingPong(SlashCommandInteractionEvent event){
        event.reply("Pong!").queue();
    }
}
