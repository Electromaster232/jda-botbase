package me.djelectro.djbot;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import me.djelectro.djbot.modules.Module;
import me.djelectro.djbot.modules.Plugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.reflections.scanners.Scanners.SubTypes;

public class BotMain {

    public static void main(String[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException, IOException, ClassNotFoundException {
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
            cpds.setJdbcUrl("jdbc:sqlite:db.sqlite");

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
        bot.awaitReady();

        // Register the core's modules
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))).forPackage("me.djelectro.snipebot").filterInputsBy(new FilterBuilder().includePackage("me.djelectro.snipebot.modules")).setScanners(SubTypes.filterResultsBy(c -> true)));
        for (Class<?> classes : reflections.get(SubTypes.of(Object.class).asClass())) {
            if(classes.getSuperclass() == Module.class){
                System.out.println("Adding " + classes.getName());
                Module c = (Module) classes.getDeclaredConstructors()[0].newInstance();
                c.register(bot);
                System.out.println("Added event listener");
                bot.addEventListener(c);
            }
        }

        // Register all sub modules
        List<Class<? extends Plugin>> classList = loadSubmoduleJars(config.getConfigValue("jarPath"));
        // All classes here are guaranteed to be subclasses of Plugin
        for(Class<? extends Plugin> c : classList){
            Plugin inst = (Plugin) c.getDeclaredConstructors()[0].newInstance();
            inst.onEnable(bot);
            for(Class<? extends Module> c2 : inst.getModules()){
                System.out.println("Adding " + c2.getName());
                Module m = (Module) c2.getDeclaredConstructors()[0].newInstance();
                m.register(bot);
                System.out.println("Added event listener");
                bot.addEventListener(m);
            }
        }



        }

    private static List<Class<? extends Plugin>> loadSubmoduleJars(String directoryPath) throws IOException, ClassNotFoundException {
        File modulesDir = new File(directoryPath);
        File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) {
            throw new IOException("No JAR files found in " + directoryPath);
        }

        List<URL> urls = new ArrayList<>();
        for (File jarFile : jarFiles) {
            urls.add(jarFile.toURI().toURL());
        }

        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), BotMain.class.getClassLoader());
        List<Class<? extends Plugin>> loadedClasses = new ArrayList<>();

        for (File jarFile : jarFiles) {
            String entryClassName = "PluginMain"; // Example Main class name
            Class<?> submoduleClass = classLoader.loadClass(entryClassName);
            for(Class<?> interfaces : submoduleClass.getInterfaces()){
                if(interfaces.isAssignableFrom(Plugin.class))
                    loadedClasses.add((Class<? extends Plugin>) submoduleClass);
            }


        }

        return loadedClasses;
    }


}
