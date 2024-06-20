package cores.tovy.github.tvaults;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Main extends JavaPlugin {
    private static Main instance;
    private Database database;
    private Gui gui;
    private Command command;
    private List<String> blockExceptions;
    private List<String> itemExceptions;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadPluginConfig();
        this.database = new Database();
        this.gui = new Gui(blockExceptions, itemExceptions);
        this.command = new Command(gui, this);
        getCommand("pv").setExecutor(command);
        getServer().getPluginManager().registerEvents(gui, this);
    }

    public static Main getInstance() {
        return instance;
    }

    public static FileConfiguration getPluginConfig() {
        return getInstance().getConfig();
    }

    public void loadPluginConfig() {
        reloadConfig();
        blockExceptions = getConfig().getStringList("block-exceptions");
        itemExceptions = getConfig().getStringList("item-exceptions");
    }

    public void reloadPluginConfig() {
        loadPluginConfig();
        this.gui = new Gui(blockExceptions, itemExceptions);
        getServer().getPluginManager().registerEvents(gui, this);
        getCommand("pv").setExecutor(new Command(gui, this));
    }

    @Override
    public void onDisable() {

    }
}