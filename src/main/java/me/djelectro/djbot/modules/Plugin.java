package me.djelectro.djbot.modules;

import net.dv8tion.jda.api.JDA;

import java.util.List;

public interface Plugin {

    public void onEnable(JDA bot);

    public void onDisable(JDA bot);

    public List<Class<? extends Module>> getModules();
}
