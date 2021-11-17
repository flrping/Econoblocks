package dev.flrp.econoblocks;

import dev.flrp.econoblocks.commands.Commands;
import dev.flrp.econoblocks.configuration.Configuration;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.listeners.BlockListeners;
import dev.flrp.econoblocks.listeners.ChunkListeners;
import dev.flrp.econoblocks.managers.*;
import me.mattstudios.mf.base.CommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Econoblocks extends JavaPlugin {

    private static Econoblocks instance;

    private Configuration blocks;
    private Configuration language;

    private BlockManager blockManager;
    private EconomyManager economyManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private HookManager hookManager;

    private final List<Player> toggleList = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        Locale.log("&8--------------");
        Locale.log("&eEconoblocks &rby flrp &8(&ev1.3.0&8)");
        Locale.log("Consider &cPatreon &rto support me for keeping these plugins free.");
        Locale.log("&8--------------");
        Locale.log("&eStarting...");

        // bStats
        Metrics metrics = new Metrics(this, 12071);

        // Files
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        initiateFiles();

        // Initiation
        Locale.load();
        initiateClasses();

        // Database things
        databaseManager = new DatabaseManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new BlockListeners(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListeners(this), this);

        // Commands
        CommandManager commandManager = new CommandManager(this);
        commandManager.register(new Commands(this));
        commandManager.getMessageHandler().register("cmd.no.permission", sender -> sender.sendMessage(Locale.parse(Locale.COMMAND_DENIED)));

        Locale.log("&eDone!");
    }

    public void onReload() {
        Locale.log("&eReloading...");
        // Files
        reloadConfig();
        initiateFiles();

        // Initiation
        Locale.load();
        initiateClasses();

        Locale.log("&eDone!");
    }

    @Override
    public void onDisable() {
        if(getConfig().getBoolean("checks.storage.enabled")) {
            databaseManager.save();
            databaseManager.closeConnection();
        }
    }

    private void initiateClasses() {
        blockManager = new BlockManager(this);
        economyManager = new EconomyManager(this);
        messageManager = new MessageManager(this);
        hookManager = new HookManager(this);
    }

    private void initiateFiles() {
        blocks = new Configuration(this);
        blocks.load("blocks");
        language = new Configuration(this);
        language.load("language");
    }

    public Configuration getBlocks() {
        return blocks;
    }

    public Configuration getLanguage() {
        return language;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public static Econoblocks getInstance() {
        return instance;
    }

    // Temporary
    public List<Player> getToggleList() {
        return toggleList;
    }

}
