package dev.flrp.econoblocks.commands;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;

@Command("econoblocks")
public class Commands extends CommandBase {

    private final Econoblocks plugin;

    public Commands(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        commandSender.sendMessage(Locale.parse("&6&lEconoblocks &7Version 1.0.1 &8| &7By flrp <3"));
        commandSender.sendMessage(Locale.parse("&f/econoblocks help &8- &7Displays this menu."));
        if(commandSender.hasPermission("econoblocks.admin")) {
            commandSender.sendMessage(Locale.parse("&f/econoblocks reload &8- &7Reloads the plugin."));
        }
    }

    @SubCommand("reload")
    @Permission("econoblocks.admin")
    public void reloadCommand(final CommandSender commandSender) {
        plugin.onReload();
        commandSender.sendMessage(Locale.parse("&aEconoblocks successfully reloaded."));
    }

}
