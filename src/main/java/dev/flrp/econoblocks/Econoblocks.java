package dev.flrp.econoblocks;

import dev.flrp.econoblocks.commands.Commands;
import dev.flrp.econoblocks.configuration.Configuration;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.listeners.BlockListeners;
import dev.flrp.econoblocks.listeners.ChunkListeners;
import dev.flrp.econoblocks.managers.*;
import dev.flrp.econoblocks.utils.UpdateChecker;
import me.mattstudios.mf.base.CommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Econoblocks extends JavaPlugin {

    private static Econoblocks instance;
    private final String version = "v1.4.1";
    private final int resourceID = 91161;

    private Configuration config;
    private Configuration blocks;
    private Configuration language;

    private BlockManager blockManager;
    private EconomyManager economyManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private HookManager hookManager;
    private MultiplierManager multiplierManager;

    private final List<UUID> toggleList = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        Locale.log("&8--------------");
        Locale.log("&eEconoblocks &rby flrp &8(&ev1.4.1&8)");
        Locale.log("Consider &cPatreon &rto support me for keeping these plugins free.");
        Locale.log("&8--------------");
        Locale.log("&eStarting...");

        // bStats
        Metrics metrics = new Metrics(this, 12071);

        // Files
        initiateFiles();

        // Initiation
        Locale.load();
        initiateClasses();

        // Check for update
        new UpdateChecker(this, resourceID).checkForUpdate(version -> {
            if(getConfig().getBoolean("check-for-updates")) {
                if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    Locale.log("&eYou are running the latest version of Econoblocks.");
                } else {
                    Locale.log("&eThere is a new version of Econoblocks available.");
                    Locale.log("&eDownload it here: &bhttps://www.spigotmc.org/resources/econoblocks.91161/");
                }
            }
        });

        // Hooks
        File dir = new File(getDataFolder(), "hooks");
        if(!dir.exists()) dir.mkdir();
        hookManager = new HookManager(this);

        // Database things
        databaseManager = new DatabaseManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new BlockListeners(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListeners(this), this);

        // Commands
        CommandManager commandManager = new CommandManager(this);
        commandManager.register(new Commands(this));
        commandManager.getMessageHandler().register("cmd.no.permission", sender -> sender.sendMessage(Locale.parse(Locale.COMMAND_DENIED)));
        commandManager.getMessageHandler().register("cmd.wrong.usage", sender -> sender.sendMessage(Locale.parse(Locale.PREFIX + "&cInvalid usage. See /econoblocks.")));

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

        // Hooks
        hookManager.reload();

        Locale.log("&eDone!");
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
    }

    private void initiateClasses() {
        blockManager = new BlockManager(this);
        economyManager = new EconomyManager(this);
        messageManager = new MessageManager(this);
        multiplierManager = new MultiplierManager(this);
    }

    private void initiateFiles() {
        config = new Configuration(this);
        config.load("config");
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

    public MultiplierManager getMultiplierManager() {
        return multiplierManager;
    }

    public static Econoblocks getInstance() {
        return instance;
    }

    public List<UUID> getToggleList() {
        return toggleList;
    }

}
