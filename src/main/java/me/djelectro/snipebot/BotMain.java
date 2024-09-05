package me.djelectro.snipebot;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import me.djelectro.snipebot.modules.Module;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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

        ComboPooledDataSource cpds = new ComboPooledDataSource();
        try {
            // Set the JDBC driver
            cpds.setDriverClass("org.sqlite.JDBC");

            // Set the JDBC URL for SQLite
            cpds.setJdbcUrl("jdbc:sqlite:/path/to/your/database.db");

            // Optional settings
            cpds.setMinPoolSize(5);
            cpds.setAcquireIncrement(5);
            cpds.setMaxPoolSize(20);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing C3P0", e);
        }

        Database.initialize(new Database(cpds));

        JDA bot = JDABuilder.createDefault(config.getConfigValue("botToken"))
                .build();

        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))).forPackage("me.djelectro.snipebot").filterInputsBy(new FilterBuilder().includePackage("me.djelectro.snipebot.commands")).setScanners(SubTypes.filterResultsBy(c -> true)));
        for (Class<?> classes : reflections.get(SubTypes.of(Object.class).asClass())) {
            if(classes.getSuperclass() == Module.class){
                System.out.println("Adding " + classes.getName());
                Module c = (Module) classes.getDeclaredConstructors()[0].newInstance();
                c.register(bot);
                System.out.println("Added event listener");
                bot.addEventListener(c);
            }
        }



        }


}
