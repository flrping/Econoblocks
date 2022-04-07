package dev.flrp.econoblocks.commands;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.utils.multiplier.MultiplierGroup;
import dev.flrp.econoblocks.utils.multiplier.MultiplierProfile;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@Command("econoblocks")
@Alias("eb")
public class Commands extends CommandBase {

    private final Econoblocks plugin;

    public Commands(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @Default
    public void defaultCommand(final CommandSender sender) {
        sender.sendMessage(Locale.parse("\n&6&lECONOBLOCKS &7Version 1.3.0 &8| &7By flrp"));
        sender.sendMessage(Locale.parse("&6/econoblocks &fhelp &8- &7Displays this menu."));
        sender.sendMessage(Locale.parse("&6/econoblocks &ftoggle &8- &7Toggles the money message."));
        if(sender.hasPermission("econoblocks.admin")) {
            sender.sendMessage(Locale.parse("&6/econoblocks &fcheck <user> &8- &7Shows the multipliers a user has."));
            sender.sendMessage(Locale.parse("&6/econoblocks &fmultiplier add <user> <block/tool/world> <context> <multiplier> &8- &7Adds a multiplier to a user."));
            sender.sendMessage(Locale.parse("&6/econoblocks &fmultiplier remove <user> <block/tool/world> <context> &8- &7Removes a multiplier from a user."));
            sender.sendMessage(Locale.parse("&6/econoblocks &freload &8- &7Reloads the plugin."));
        }
    }

    @SubCommand("help")
    public void helpCommand(final CommandSender sender) {
        defaultCommand(sender);
    }

    @SubCommand("toggle")
    @Alias("togglemessage")
    public void toggleCommand(final CommandSender sender) {
        Player player = (Player) sender;
        send(sender, Locale.ECONOMY_TOGGLE);
        if(!plugin.getToggleList().contains(player)) {
            plugin.getToggleList().add(player);
            return;
        }
         plugin.getToggleList().remove(player);
    }

    @SubCommand("multiplier")
    @Permission("econoblocks.admin")
    public void multiplierCommand(final CommandSender sender, final String[] args) {
        if(args.length < 5) {
            send(sender, "&cInvalid usage. See /econoblocks.");
            return;
        }

        Player recipient = Bukkit.getPlayer(args[2]);
        if(recipient == null) {
            send(sender, "&4" + args[2] + " is not a valid player.");
            return;
        }

        double multiplier = 1;
        if(args.length == 6) {
            try {
                multiplier = Double.parseDouble(args[5]);
            } catch (NumberFormatException e) {
                send(sender, "&4" + args[5] + " &cis not a valid number.");
                return;
            }
        }

        if((multiplier == 1 && args[1].equals("add"))) {
            send(sender, "&cInvalid multiplier. Please add a value that modifies the base amount.");
            return;
        }

        MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(recipient.getUniqueId());

        if(args[3].equals("block") || args[3].equals("tool")) {
            Material material = Material.matchMaterial(args[4]);
            if(material == null) {
                send(sender, "&4" + args[4] + " &cis not a valid material.");
                return;
            }
            if(args[1].equals("add")) {
                if (args[3].equals("block")) {
                    multiplierProfile.addBlockMultiplier(material, multiplier);
                } else {
                    multiplierProfile.addToolMultiplier(material, multiplier);
                }
                send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ", " + multiplier + ").");
                return;
            }
            if(args[1].equals("remove")) {
                if (args[3].equals("block")) {
                    if(!multiplierProfile.getMaterials().containsKey(material)) {
                        send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                        return;
                    }
                    multiplierProfile.removeBlockMultiplier(material);
                } else {
                    if(!multiplierProfile.getTools().containsKey(material)) {
                        send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                        return;
                    }
                    multiplierProfile.removeToolMultiplier(material);
                }
                send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ").");
                return;
            }
            send(sender, "&cInvalid usage. See /econoblocks.");
        } else
        if(args[3].equals("world")) {
            UUID world = Bukkit.getWorld(args[4]) != null ? Bukkit.getWorld(args[4]).getUID() : null;
            if(world == null) {
                send(sender, "&4" + args[4] + " &cis not a valid world.");
                return;
            }
            if(args[1].equals("add")) {
                multiplierProfile.addWorldMultiplier(world, multiplier);
                send(sender, "&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ", " + multiplier + ").");
                return;
            }
            if(args[1].equals("remove")) {
                if(!multiplierProfile.getWorlds().containsKey(world)) {
                    send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                    return;
                }
                multiplierProfile.removeWorldMultiplier(world);
                send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ").");
                return;
            }
        }
        send(sender, "&cInvalid usage. See /econoblocks.");
    }

    @SubCommand("check")
    @Permission("econoblocks.admin")
    public void checkCommand(final CommandSender sender, final Player player) {
        MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

        sender.sendMessage(Locale.parse("\n&6&lMULTIPLIER PROFILE"));
        sender.sendMessage(Locale.parse("&7Username: &f" + player.getName()));
        sender.sendMessage(Locale.parse("&7Group: &f" + (group != null ? group.getIdentifier() : "N/A")));

        sender.sendMessage(Locale.parse("&7Block Multipliers:"));
        if(!multiplierProfile.getMaterials().isEmpty()) multiplierProfile.getMaterials().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getMaterials().forEach((key, value) -> {
                if(!multiplierProfile.getMaterials().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
            });
        }

        sender.sendMessage(Locale.parse("&7Tool Multipliers:"));
        if(!multiplierProfile.getTools().isEmpty()) multiplierProfile.getTools().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 : &ax" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getTools().forEach((key, value) -> {
                if(!multiplierProfile.getTools().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
            });
        }

        sender.sendMessage(Locale.parse("&7World Multipliers:"));
        if(!multiplierProfile.getWorlds().isEmpty()) multiplierProfile.getWorlds().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getWorlds().forEach((key, value) -> {
                if(!multiplierProfile.getWorlds().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
            });
        }
    }

    @SubCommand("reload")
    @Permission("econoblocks.admin")
    public void reloadCommand(final CommandSender sender) {
        plugin.onReload();
        send(sender, "&aEconoblocks successfully reloaded.");
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(Locale.parse(Locale.PREFIX + message));
    }

}
