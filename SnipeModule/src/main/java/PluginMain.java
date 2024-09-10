import me.djelectro.djbot.modules.Module;
import me.djelectro.djbot.modules.Plugin;
import me.djelectro.djbot.snipemodule.Snipe;
import net.dv8tion.jda.api.JDA;

import java.util.List;

public class PluginMain implements Plugin {
    @Override
    public void onEnable(JDA bot) {
        // Nothing to do
    }

    @Override
    public void onDisable(JDA bot) {
        // Nothing to do
    }

    @Override
    public List<Class<? extends Module>> getModules() {
        return List.of(Snipe.class);
    }
}
