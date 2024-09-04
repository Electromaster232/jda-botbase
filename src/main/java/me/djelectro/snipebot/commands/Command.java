package me.djelectro.snipebot.commands;

import me.djelectro.snipebot.annotations.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class Command implements EventListener {

    HashMap<String, Method> slashCommands = new HashMap<>();

    public void registerCommands(JDA jda) {
        Method[] methods = this.getClass().getDeclaredMethods();

        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof SlashCommand) {
                    slashCommands.put(((SlashCommand) annotation).name(), method);
                    List<OptionData> resList = new ArrayList<>();
                    Arrays.stream(((SlashCommand) annotation).options()).toList().forEach(item -> {
                        resList.add(new OptionData(item.option(), item.name(), item.description()));
                    });
                    System.out.println("Adding command " + ((SlashCommand) annotation).name() );
                    jda.updateCommands().addCommands(Commands.slash(((SlashCommand) annotation).name(), ((SlashCommand) annotation).description()).addOptions(resList)).queue();
                }
            }
        }
    }

    public void onEvent(GenericEvent event){
        // TODO: Refactor at a time other than 1am
        switch(event){
            case SlashCommandInteractionEvent sie -> {
                SlashCommandInteractionEvent eV = (SlashCommandInteractionEvent) sie;
                System.out.println("Slash Command Handled");
                try {
                    slashCommands.get(eV.getName()).invoke(this, event);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> System.out.println(this.getClass().getName() + " Could not handle event " + event.getClass().getName());
        }

    }

}
