package me.djelectro.snipebot;

import me.djelectro.snipebot.commands.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import static org.reflections.scanners.Scanners.SubTypes;

public class BotMain {

    public static void main(String[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());
        Config config = null;
        try {
            config = new Config(args[0]);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        JDA bot = JDABuilder.createDefault(config.getConfigValue("botToken"))
                .build();

        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))).forPackage("me.djelectro.snipebot").filterInputsBy(new FilterBuilder().includePackage("me.djelectro.snipebot.commands")).setScanners(SubTypes.filterResultsBy(c -> true)));
        for (Class<?> classes : reflections.get(SubTypes.of(Object.class).asClass())) {
            if(classes.getSuperclass() == Command.class){
                System.out.println("Adding " + classes.getName());
                Command c = (Command) classes.getDeclaredConstructors()[0].newInstance();
                c.registerCommands(bot);
                System.out.println("Added event listener");
                bot.addEventListener(c);
            }
        }



        }


}
