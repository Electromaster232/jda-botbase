package me.djelectro.snipebot.commands;

import me.djelectro.snipebot.annotations.EventHandler;
import me.djelectro.snipebot.annotations.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Module implements EventListener {

    HashMap<String, Method> slashCommands = new HashMap<>();

    HashMap<Method, Class<? extends GenericEvent>> eventReceivers = new HashMap<>();

    public void register(JDA jda) {
        Method[] methods = this.getClass().getMethods();

        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof SlashCommand sla) {
                    slashCommands.put(sla.name(), method);
                    List<OptionData> resList = new ArrayList<>();
                    Arrays.stream(sla.options()).toList().forEach(item -> {
                        resList.add(new OptionData(item.option(), item.name(), item.description()));
                    });
                    System.out.println("Adding command " + sla.name() );
                    jda.upsertCommand(Commands.slash(sla.name(), sla.description()).addOptions(resList)).queue();
                }
                else if (annotation instanceof EventHandler){
                    // This is unimaginably cursed
                    if(GenericEvent.class.isAssignableFrom(method.getParameterTypes()[0]))
                        eventReceivers.put(method, (Class<? extends GenericEvent>) method.getParameterTypes()[0]);
                }
            }
        }
    }

    public void onEvent(@NotNull GenericEvent event){
        AtomicBoolean success = new AtomicBoolean(false);
        eventReceivers.forEach((k,v) -> {
            if(event.getClass() == v){
                try {
                    k.invoke(this, event);
                    success.set(true);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        if(!success.get())
            System.out.println(this.getClass().getName() + " could not handle event " + event.getClass().getName());
    }

    @EventHandler
    public void onSlashCommand(SlashCommandInteractionEvent eV){
        System.out.println("Slash Command Handled");
        try {
            slashCommands.get(eV.getName()).invoke(this, eV);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
