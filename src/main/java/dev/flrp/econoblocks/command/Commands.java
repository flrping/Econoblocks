package dev.flrp.econoblocks.command;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.hook.block.ItemsAdderBlockHook;
import dev.flrp.econoblocks.hook.block.OraxenBlockHook;
import dev.flrp.econoblocks.util.multiplier.MultiplierGroup;
import dev.flrp.econoblocks.util.multiplier.MultiplierProfile;
import dev.flrp.espresso.condition.BiomeCondition;
import dev.flrp.espresso.condition.Condition;
import dev.flrp.espresso.condition.WithConditionExtended;
import dev.flrp.espresso.condition.WorldCondition;
import dev.flrp.espresso.hook.block.BlockProvider;
import dev.flrp.espresso.hook.item.ItemType;
import dev.flrp.espresso.table.LootContainer;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@Command(value = "econoblocks", alias = {"eb"})
public class Commands extends BaseCommand {

    private final Econoblocks plugin;

    public Commands(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @Default
    public void defaultCommand(final CommandSender sender) {
        sender.sendMessage(Locale.parse("\n&6&lECONOBLOCKS &7Version " + plugin.getDescription().getVersion() + " &8| &7By flrp"));
        sender.sendMessage(Locale.parse("&6/econoblocks &fhelp &8- &7Displays this menu."));
        if(sender.hasPermission("econoblocks.toggle")) sender.sendMessage(Locale.parse("&6/econoblocks &ftoggle &8- &7Toggles the money message."));
        if(sender.hasPermission("econoblocks.profile")) sender.sendMessage(Locale.parse("&6/econoblocks &fprofile <user> &8- &7Shows the multipliers a user has."));
        if(sender.hasPermission("econoblocks.check")) sender.sendMessage(Locale.parse("&6/econoblocks &fcheck <block/custom> <name> &8- &7Shows the loot profile of a block."));
        if(sender.hasPermission("econoblocks.multiplier")) {
            sender.sendMessage(Locale.parse("&6/econoblocks &fmultiplier add <user> <block/tool/world/custom_block/custom_tool> <context> <multiplier> &8- &7Adds a multiplier to a user."));
            sender.sendMessage(Locale.parse("&6/econoblocks &fmultiplier remove <user> <block/tool/world/custom_block/custom_tool> <context> &8- &7Removes a multiplier from a user."));
        }
        if(sender.hasPermission("econoblocks.reload")) sender.sendMessage(Locale.parse("&6/econoblocks &freload &8- &7Reloads the plugin."));
    }

    @SubCommand("help")
    public void helpCommand(final CommandSender sender) {
        defaultCommand(sender);
    }

    @SubCommand("multiplier")
    @Permission("econoblocks.multiplier")
    public void multiplierCommand(final CommandSender sender, final List<String> args) {

        String action = args.get(0);
        String player = args.get(1);
        String type = args.get(2);
        String context = args.get(3);
        double multiplier = 1;

        Player recipient = Bukkit.getPlayer(player);
        if(recipient == null) {
            send(sender, "&4" + player + " is not a valid player.");
            return;
        }

        if(args.size() == 5) {
            try {
                multiplier = Double.parseDouble(args.get(4));
            } catch (NumberFormatException e) {
                send(sender, "&4" + args.get(4) + " &cis not a valid number.");
                return;
            }
        }

        if((multiplier == 1 && action.equals("add"))) {
            send(sender, "&cInvalid multiplier. Please add a value that modifies the base amount.");
            return;
        }

        MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(recipient.getUniqueId());

        switch (type) {
            case "block":
                handleBlockMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            case "tool":
                handleToolMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            case "world":
                handleWorldMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            case "custom_block":
                handleCustomBlockMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            case "custom_tool":
                handleCustomToolMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            default:
                send(sender, "&cInvalid usage. See /econoblocks.");
        }
    }

    private void handleBlockMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        Material material;
        try {
            material = Material.valueOf(context.toUpperCase());
        } catch (IllegalArgumentException e) {
            send(sender, "&4" + context + " &cis not a valid material.");
            return;
        }
        if (action.equals("add")) {
            multiplierProfile.addBlockMultiplier(material, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + context + ", " + multiplier + ").");
            return;
        }
        if (action.equals("remove")) {
            if(!multiplierProfile.getMaterials().containsKey(material)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeBlockMultiplier(material);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + context + ").");
            return;
        }
        send(sender, "&cInvalid usage. See /econoblocks.");
    }

    private void handleToolMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        Material material;
        try {
            material = Material.valueOf(context.toUpperCase());
        } catch (IllegalArgumentException e) {
            send(sender, "&4" + context + " &cis not a valid material.");
            return;
        }
        if (action.equals("add")) {
            multiplierProfile.addToolMultiplier(material, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + context + ", " + multiplier + ").");
            return;
        }
        if (action.equals("remove")) {
            if(!multiplierProfile.getTools().containsKey(material)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeToolMultiplier(material);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + context + ").");
            return;
        }
        send(sender, "&cInvalid usage. See /econoblocks.");
    }

    private void handleWorldMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        UUID world = Bukkit.getWorld(context) != null ? Bukkit.getWorld(context).getUID() : null;
        if (world == null) {
            send(sender, "&4" + context + " &cis not a valid world.");
            return;
        }
        if (action.equals("add")) {
            multiplierProfile.addWorldMultiplier(world, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + context + ", " + multiplier + ").");
            return;
        }
        if (action.equals("remove")) {
            if(!multiplierProfile.getWorlds().containsKey(world)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeWorldMultiplier(world);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + context + ").");
        }
    }

    private void handleCustomBlockMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        if (action.equals("add")) {
            multiplierProfile.addCustomBlockMultiplier(context, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + context + ", " + multiplier + ").");
            return;
        }
        if (action.equals("remove")) {
            if(!multiplierProfile.getCustomMaterials().containsKey(context)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeCustomBlockMultiplier(context);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + context + ").");
        }
    }

    private void handleCustomToolMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        if (action.equals("add")) {
            multiplierProfile.addCustomToolMultiplier(context, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + context + ", " + multiplier + ").");
            return;
        }
        if (action.equals("remove")) {
            if(!multiplierProfile.getCustomTools().containsKey(context)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeCustomToolMultiplier(context);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + context + ").");
        }
    }

    @SubCommand("check")
    @Permission("econoblocks.check")
    public void checkCommand(final CommandSender sender, final String type, final String target) {
        switch(type) {
            case "block":
                handleBlockCheck(sender, target);
                break;
            case "custom":
                handleCustomBlockCheck(sender, target);
                break;
            default:
                send(sender, "&cInvalid usage. See /econoblocks.");
        }
    }

    private void handleBlockCheck(CommandSender sender, String blockName) {
        Material material;
        try {
            material = Material.valueOf(blockName.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Locale.parse("&cInvalid material: " + blockName));
            return;
        }
        LootContainer container;
        if(plugin.getRewardManager().hasLootContainer(blockName)) {
            container = plugin.getRewardManager().getLootContainer(blockName);
        } else {
            container = plugin.getRewardManager().getDefaultLootContainer();
            if(container.getLootTables().isEmpty()) {
                sender.sendMessage(Locale.parse("&cNo loot tables found for this block."));
                return;
            }
        }
        generateInfo(sender, material.name(), container);
    }

    private void handleCustomBlockCheck(final CommandSender sender, final String target) {
        BlockProvider provider = plugin.getHookManager().getBlockProviders().stream().filter(p -> p.isCustomBlock(target)).findFirst().orElse(null);
        if (provider == null) {
            sender.sendMessage(Locale.parse("&cCustom block not found: " + target));
            return;
        }
        LootContainer container;
        switch (provider.getType()) {
            case ITEMS_ADDER:
                if(((ItemsAdderBlockHook) provider).hasLootContainer(target)) {
                    container = ((ItemsAdderBlockHook) provider).getLootContainer(target);
                } else {
                    container = ((ItemsAdderBlockHook) provider).getDefaultLootContainer();
                    if(container.getLootTables().isEmpty()) {
                        sender.sendMessage(Locale.parse("&cNo loot tables found for this block."));
                        return;
                    }
                }
                generateInfo(sender, target, container);
                break;
            case ORAXEN:
                if(((OraxenBlockHook) provider).hasLootContainer(target)) {
                    container = ((OraxenBlockHook) provider).getLootContainer(target);
                } else {
                    container = ((OraxenBlockHook) provider).getDefaultLootContainer();
                    if(container.getLootTables().isEmpty()) {
                        sender.sendMessage(Locale.parse("&cNo loot tables found for this block."));
                        return;
                    }
                }
                generateInfo(sender, target, container);
                break;
        }
    }

    private void generateInfo(CommandSender sender, String block, LootContainer container) {
        sender.sendMessage(Locale.parse("\n&6&lLOOT PROFILE &7(" + block + ")"));
        container.getLootTables().forEach((tableName, table) -> {
            sender.sendMessage(Locale.parse("\n&7Table: &f" + tableName));
            double chance = Math.round(table.getWeight() / container.getTotalWeightOfTables() * 10000.0) / 100.0;
            sender.sendMessage(Locale.parse("&7Weight: &f" + table.getWeight() + " &7(&6" + chance + "%&7)"));
            sender.sendMessage(Locale.parse("&7Conditions:"));
            for(Condition condition : table.getConditions()) {
                switch (condition.getType()) {
                    case WITH:
                        generateWithInfo(sender, (WithConditionExtended) condition);
                        break;
                    case BIOME:
                        sender.sendMessage(Locale.parse(" &7Biome:"));
                        ((BiomeCondition) condition).getBiomes().forEach(biome -> sender.sendMessage(Locale.parse(" &8 - &f" + biome.name())));
                        break;
                    case WORLD:
                        sender.sendMessage(Locale.parse(" &7World:"));
                        ((WorldCondition) condition).getWorlds().forEach(world -> sender.sendMessage(Locale.parse(" &8 - &f" + world)));
                        break;
                    case PERMISSION:
                        sender.sendMessage(Locale.parse(" &7Permission:"));
                        sender.sendMessage(Locale.parse(" &8 - &f" + ((dev.flrp.espresso.condition.PermissionCondition) condition).getPermission()));
                        break;
                }
            }
            sender.sendMessage(Locale.parse("&6Possible Drops:"));
            double chanceOfNothing = table.getEntryTotalWeight() < 100 ? Math.round((100 - table.getEntryTotalWeight()) * 100.0) / 100.0 : 0;
            sender.sendMessage(Locale.parse("&7 No Reward Chance: &c" + chanceOfNothing + "%"));
            table.getLoots().forEach((lootName, loot) -> {
                double lootChance = Math.round(loot.getWeight() / table.getEntryTotalWeight() * 10000.0) / 100.0;
                double lootActualChance = Math.round((loot.getWeight() / table.getEntryTotalWeight()) * (table.getWeight() / container.getTotalWeightOfTables()) * 10000.0) / 100.0;
                sender.sendMessage(Locale.parse(" &7ID: &f" + lootName + " &8&o(" + loot.getType().name() + ")"));
                sender.sendMessage(Locale.parse(" &7Weight: &f" + loot.getWeight() + " &7(&6" + lootChance + "% &8| &6" + lootActualChance + "%&7)"));
            });
        });
    }

    private void generateWithInfo(CommandSender sender, WithConditionExtended condition) {
        sender.sendMessage(Locale.parse(" &7With:"));
        condition.getMaterials().forEach((key, value) -> {
            String item = value.toString().replace("[", "").replace("]", "");
            if(key != ItemType.NONE) {
                sender.sendMessage(Locale.parse(" &8 - &f" + item + " &7&o(" + key.name() + ")"));
            } else {
                sender.sendMessage(Locale.parse(" &8 - &f" + item));
            }
        });
    }

    @SubCommand("profile")
    @Permission("econoblocks.profile")
    public void profileCommand(final CommandSender sender, final Player player) {
        MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

        sender.sendMessage(Locale.parse("\n&6&lMULTIPLIER PROFILE"));
        sender.sendMessage(Locale.parse("&7Username: &f" + player.getName()));
        sender.sendMessage(Locale.parse("&7Group: &f" + (group != null ? group.getIdentifier() + " &6(Weight: " + group.getWeight() + ")": "N/A")));

        sender.sendMessage(Locale.parse("&7Block Multipliers:"));
        if(!multiplierProfile.getMaterials().isEmpty()) multiplierProfile.getMaterials().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getMaterials().forEach((key, value) -> {
                if(!multiplierProfile.getMaterials().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 GROUP"));
            });
        }

        sender.sendMessage(Locale.parse("&7Tool Multipliers:"));
        if(!multiplierProfile.getTools().isEmpty()) multiplierProfile.getTools().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 : &6x" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getTools().forEach((key, value) -> {
                if(!multiplierProfile.getTools().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 GROUP"));
            });
        }

        sender.sendMessage(Locale.parse("&7World Multipliers:"));
        if(!multiplierProfile.getWorlds().isEmpty()) multiplierProfile.getWorlds().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getWorlds().forEach((key, value) -> {
                if(!multiplierProfile.getWorlds().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 GROUP"));
            });
        }
        sender.sendMessage(Locale.parse("&7Custom Block Multipliers:"));
        if(!multiplierProfile.getCustomMaterials().isEmpty()) multiplierProfile.getCustomMaterials().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getCustomMaterials().forEach((key, value) -> {
                if(!multiplierProfile.getCustomMaterials().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 GROUP"));
            });
        }
        sender.sendMessage(Locale.parse("&7Custom Tool Multipliers:"));
        if(!multiplierProfile.getCustomTools().isEmpty()) multiplierProfile.getCustomTools().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getCustomTools().forEach((key, value) -> {
                if(!multiplierProfile.getCustomTools().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &6x" + value + "&8 |&7 GROUP"));
            });
        }
    }


    @SubCommand(value = "toggle", alias = {"togglemessage"})
    @Permission("econoblocks.toggle")
    public void toggleCommand(final CommandSender sender) {
        Player player = (Player) sender;
        send(sender, Locale.ECONOMY_TOGGLE);
        if(!plugin.getToggleList().contains(player.getUniqueId())) {
            plugin.getToggleList().add(player.getUniqueId());
            return;
        }
        plugin.getToggleList().remove(player.getUniqueId());
    }


    @SubCommand("reload")
    @Permission("econoblocks.admin")
    public void reloadCommand(final CommandSender sender) {
        plugin.onReload();
        send(sender, "&7Econoblocks successfully reloaded.");
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(Locale.parse(Locale.PREFIX + message));
    }

}
