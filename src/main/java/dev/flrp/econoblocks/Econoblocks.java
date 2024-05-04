package dev.flrp.econoblocks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.flrp.econoblocks.command.Commands;
import dev.flrp.econoblocks.configuration.Builder;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.listener.BlockListener;
import dev.flrp.econoblocks.listener.ChunkListener;
import dev.flrp.econoblocks.manager.*;
import dev.flrp.econoblocks.module.BlockModule;
import dev.flrp.econoblocks.module.EconomyModule;
import dev.flrp.econoblocks.module.HologramModule;
import dev.flrp.econoblocks.module.ItemModule;
import dev.flrp.econoblocks.placeholder.EconoblocksExpansion;
import dev.flrp.econoblocks.util.UpdateChecker;
import dev.flrp.espresso.configuration.Configuration;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.message.MessageKey;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Econoblocks extends JavaPlugin {

    private static Econoblocks instance;

    private Configuration config;
    private Configuration blocks;
    private Configuration lootTables;
    private Configuration language;

    private RewardManager rewardManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private HookManager hookManager;
    private MultiplierManager multiplierManager;

    private final List<UUID> toggleList = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        Locale.log("&8--------------");
        Locale.log("&eEconoblocks &rby flrp &8(&ev" + this.getDescription().getVersion() + "&8)");
        Locale.log("Consider &cKo-fi &rto support me for keeping these plugins free.");
        Locale.log("&8--------------");
        Locale.log("&eStarting...");

        // bStats
        Metrics metrics = new Metrics(this, 12071);

        // Files
        initiateFiles();

        // Initiation
        Locale.load();
        initiateClasses();

        // Modules
        Injector hookInjector = Guice.createInjector(new BlockModule(this), new EconomyModule(this), new HologramModule(this), new ItemModule(this));
        hookManager = hookInjector.getInstance(HookManager.class);

        // Check for update
        int resourceID = 91161;
        new UpdateChecker(this, resourceID).checkForUpdate(version -> {
            if(getConfig().getBoolean("check-for-updates")) {
                if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    Locale.log("&8--------------");
                    Locale.log("There is a new version of Econoblocks available.");
                    Locale.log("Download it here:&e https://www.spigotmc.org/resources/econoblocks.91161/");
                    Locale.log("&8--------------");
                }
            }
        });

        // Hooks
        File dir = new File(getDataFolder(), "hooks");
        if(!dir.exists()) dir.mkdir();

        // Database things
        databaseManager = new DatabaseManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);

        // Commands
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new Commands(this));
        commandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> sender.sendMessage(Locale.parse(Locale.PREFIX + Locale.COMMAND_DENIED)));
        commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> sender.sendMessage(Locale.parse(Locale.PREFIX + "&cInvalid usage. See /econoblocks.")));

        // Placeholder
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EconoblocksExpansion(this).register();
        }

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
        hookManager.getBlockProviders().forEach(provider -> {
            ((Builder) provider).reload();
        });

        Locale.log("&eDone!");
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
    }

    private void initiateClasses() {
        rewardManager = new RewardManager(this);
        messageManager = new MessageManager(this);
        multiplierManager = new MultiplierManager(this);
    }

    private void initiateFiles() {
        config = new Configuration(this, "config");
        blocks = new Configuration(this, "blocks");
        language = new Configuration(this, "language");
        lootTables = new Configuration(this, "loot");
    }

    public Configuration getBlocks() {
        return blocks;
    }

    public Configuration getLootTables() {
        return lootTables;
    }

    public Configuration getLanguage() {
        return language;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
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
