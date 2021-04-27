package dev.flrp.econoblocks;

import dev.flrp.econoblocks.commands.Commands;
import dev.flrp.econoblocks.configuration.Configuration;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.listeners.BlockListeners;
import dev.flrp.econoblocks.managers.BlockManager;
import dev.flrp.econoblocks.managers.EconomyManager;
import dev.flrp.econoblocks.managers.MessageManager;
import me.mattstudios.mf.base.CommandManager;
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

    private final List<Player> toggleList = new ArrayList<>();

    @Override
    public void onEnable() {
        System.out.println("[Econoblocks] Starting...");
        instance = this;

        // Files
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        initiateFiles();

        // Initiation
        Locale.load();
        initiateClasses();

        // Listeners
        getServer().getPluginManager().registerEvents(new BlockListeners(this), this);

        // Commands
        CommandManager commandManager = new CommandManager(this);
        commandManager.register(new Commands(this));
        commandManager.getMessageHandler().register("cmd.no.permission", sender -> sender.sendMessage(Locale.parse(Locale.COMMAND_DENIED)));

        System.out.println("[Econoblocks] Done!");
    }

    public void onReload() {
        System.out.println("[Econoblocks] Reloading...");
        // Files
        reloadConfig();
        initiateFiles();

        // Initiation
        Locale.load();
        initiateClasses();

        System.out.println("[Econoblocks] Done!");
    }

    private void initiateClasses() {
        blockManager = new BlockManager(this);
        economyManager = new EconomyManager(this);
        messageManager = new MessageManager(this);
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

    public static Econoblocks getInstance() {
        return instance;
    }

    // Temporary
    public List<Player> getToggleList() {
        return toggleList;
    }

}