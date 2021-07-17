package dev.flrp.econoblocks.commands;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("econoblocks")
public class Commands extends CommandBase {

    private final Econoblocks plugin;

    public Commands(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        commandSender.sendMessage(Locale.parse("&6&lEconoblocks &7Version 1.2.3 &8| &7By flrp"));
        commandSender.sendMessage(Locale.parse("&f/econoblocks help &8- &7Displays this menu."));
        commandSender.sendMessage(Locale.parse("&f/econoblocks toggle &8- &7Toggles the money message."));
        if(commandSender.hasPermission("econoblocks.admin")) {
            commandSender.sendMessage(Locale.parse("&f/econoblocks reload &8- &7Reloads the plugin."));
        }
    }

    @SubCommand("toggle")
    @Alias("togglemessage")
    public void toggleCommand(final CommandSender commandSender) {
        Player player = (Player) commandSender;
        commandSender.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_TOGGLE));
        if(!plugin.getToggleList().contains(player)) {
            plugin.getToggleList().add(player);
            return;
        }
         plugin.getToggleList().remove(player);
    }

    @SubCommand("reload")
    @Permission("econoblocks.admin")
    public void reloadCommand(final CommandSender commandSender) {
        plugin.onReload();
        commandSender.sendMessage(Locale.parse(Locale.PREFIX + "&aEconoblocks successfully reloaded."));
    }

}
