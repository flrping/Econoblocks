package dev.flrp.econoblocks.util;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.util.multiplier.MultiplierGroup;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.table.LootContainer;
import dev.flrp.espresso.table.LootTable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Methods {

    private static final Econoblocks instance = Econoblocks.getInstance();

    public static ItemStack itemInHand(Player player) {
        if(instance.getServer().getVersion().contains("1.8")) {
            return player.getItemInHand();
        }
        return player.getInventory().getItemInMainHand();
    }

    public static void buildHookMultipliersBlocks(Configuration configuration) {
        if(configuration.getConfiguration().getConfigurationSection("multipliers") == null) {
            configuration.getConfiguration().set("multipliers.example.blocks", new ArrayList<>(Collections.singletonList("amethyst_ore 1.2")));
        }
    }

    public static void buildHookMultipliersItems(Configuration configuration) {
        if(configuration.getConfiguration().contains("multipliers.example") && !configuration.getConfiguration().contains("multipliers.example.items")) {
            configuration.getConfiguration().set("multipliers.example.items", new ArrayList<>(Collections.singletonList("emerald_pickaxe 1.2")));
        }
    }

    public static void buildHookBlocks(Configuration configuration) {
        if(configuration.getConfiguration().getConfigurationSection("blocks") == null) {
            configuration.getConfiguration().createSection("blocks.amethyst_ore.tables.1");
            configuration.getConfiguration().set("blocks.amethyst_ore.tables.1.table", "money_table");
        }
    }

    public static void buildHookBlocks(Configuration configuration, List<String> blockNames) {
        if(configuration.getConfiguration().getConfigurationSection("blocks") == null) {
            if(blockNames.isEmpty()) {
                configuration.getConfiguration().createSection("blocks.amethyst_ore.tables.1");
                configuration.getConfiguration().set("blocks.amethyst_ore.tables.1.table", "money_table");
                return;
            }
            for(String blockName : blockNames) {
                configuration.getConfiguration().createSection("blocks." + blockName + ".tables.1");
                configuration.getConfiguration().set("blocks." + blockName + ".tables.1.table", "money_table");
            }
        }
    }

    public static void buildHookMultiplierGroupsBlocks(Configuration configuration) {
        if(configuration.getConfiguration().getConfigurationSection("multipliers") == null) return;

        Set<String> multiplierSet = configuration.getConfiguration().getConfigurationSection("multipliers").getKeys(false);
        for (String multiplier : multiplierSet) {
            MultiplierGroup multiplierGroup;
            if(instance.getMultiplierManager().isMultiplierGroup(multiplier)) {
                multiplierGroup = instance.getMultiplierManager().getMultiplierGroupByName(multiplier);
            } else {
                multiplierGroup = new MultiplierGroup(multiplier);
                int weight = configuration.getConfiguration().contains("multipliers." + multiplier + ".weight") ? configuration.getConfiguration().getInt("multipliers." + multiplier + ".weight") : 0;
                multiplierGroup.setWeight(weight);
                instance.getMultiplierManager().addMultiplierGroup(multiplier, multiplierGroup);
            }

            for (String entry : configuration.getConfiguration().getStringList("multipliers." + multiplier + ".blocks")) {
                try {
                    String blockName = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomMaterialMultiplier(blockName, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }

        }
    }

    public static void buildHookMultiplierGroupsItems(Configuration configuration) {
        if(configuration.getConfiguration().getConfigurationSection("multipliers") == null) return;

        Set<String> multiplierSet = configuration.getConfiguration().getConfigurationSection("multipliers").getKeys(false);
        for (String multiplier : multiplierSet) {
            MultiplierGroup multiplierGroup;
            if(instance.getMultiplierManager().isMultiplierGroup(multiplier)) {
                multiplierGroup = instance.getMultiplierManager().getMultiplierGroupByName(multiplier);
            } else {
                multiplierGroup = new MultiplierGroup(multiplier);
                int weight = configuration.getConfiguration().contains("multipliers." + multiplier + ".weight") ? configuration.getConfiguration().getInt("multipliers." + multiplier + ".weight") : 0;
                multiplierGroup.setWeight(weight);
                instance.getMultiplierManager().addMultiplierGroup(multiplier, multiplierGroup);
            }

            for (String entry : configuration.getConfiguration().getStringList("multipliers." + multiplier + ".items")) {
                try {
                    String toolName = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomToolMultiplier(toolName, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }
        }
    }

    public static void buildRewardList(Configuration configuration, HashMap<String, LootContainer> rewards, String name) {
        if(configuration.getConfiguration().getConfigurationSection("blocks") == null) return;

        int modifiedTables = 0;
        Set<String> blockSet = configuration.getConfiguration().getConfigurationSection("blocks").getKeys(false);

        for(String block : blockSet) {

            LootContainer lootContainer = new LootContainer();

            Set<String> tableSet = configuration.getConfiguration().getConfigurationSection("blocks." + block + ".tables").getKeys(false);
            for(String tableNumber : tableSet) {

                // Boolean checks
                boolean hasTable = configuration.getConfiguration().contains("blocks." + block + ".tables." + tableNumber + ".table")
                        && instance.getRewardManager().getLootTables().containsKey(configuration.getConfiguration().getString("blocks." + block + ".tables." + tableNumber + ".table"));
                boolean hasConditions = configuration.getConfiguration().contains("blocks." + block + ".tables." + tableNumber + ".conditions");
                boolean hasWeightOverride = configuration.getConfiguration().contains("blocks." + block + ".tables." + tableNumber + ".weight");

                if(!hasTable) continue;
                LootTable lootTable = instance.getRewardManager().getLootTables().get(configuration.getConfiguration().getString("blocks." + block + ".tables." + tableNumber + ".table"));

                if(!hasConditions && !hasWeightOverride) {
                    lootContainer.addLootTable(lootTable);
                } else {
                    LootTable modifiedLootTable = lootTable.clone();
                    if(hasConditions) instance.getRewardManager().parseConditions(modifiedLootTable, configuration.getConfiguration().getConfigurationSection("blocks." + block + ".tables." + tableNumber));
                    if(hasWeightOverride) modifiedLootTable.setWeight(configuration.getConfiguration().getDouble("blocks." + block + ".tables." + tableNumber + ".weight"));
                    lootContainer.addLootTable(modifiedLootTable);
                    modifiedTables++;
                }

            }

            rewards.put(block, lootContainer);
        }

        Locale.log("Loaded &e" + rewards.size() + " &rloot containers for " + name + " blocks.");
        Locale.log("Loaded &e" + modifiedTables + " &rmodified loot tables.");
    }

    public static void buildDefaultLootContainer(Configuration configuration, LootContainer lootContainer, List<String> excludedEntities) {
        if(configuration.getConfiguration().getConfigurationSection("default") == null) return;

        Set<String> tableSet = configuration.getConfiguration().getConfigurationSection("default.tables").getKeys(false);
        for(String tableNumber : tableSet) {
            LootTable lootTable = instance.getRewardManager().getLootTables().get(configuration.getConfiguration().getString("default.tables." + tableNumber + ".table"));
            if(lootTable == null) continue;

            boolean hasConditions = configuration.getConfiguration().contains("default.tables." + tableNumber + ".conditions");
            boolean hasWeightOverride = configuration.getConfiguration().contains("default.tables." + tableNumber + ".weight");

            if(!hasConditions && !hasWeightOverride) {
                lootContainer.addLootTable(lootTable);
            } else {
                LootTable modifiedLootTable = lootTable.clone();
                if(hasConditions) instance.getRewardManager().parseConditions(modifiedLootTable, configuration.getConfiguration().getConfigurationSection("default.tables." + tableNumber));
                if(hasWeightOverride) modifiedLootTable.setWeight(configuration.getConfiguration().getDouble("default.tables." + tableNumber + ".weight"));
                lootContainer.addLootTable(modifiedLootTable);
            }
        }

        if(configuration.getConfiguration().contains("default.excludes")) {
            excludedEntities.addAll(configuration.getConfiguration().getStringList("default.excluded"));
        }

        Locale.log("Default loot created with &e" + lootContainer.getLootTables().size() + " &rtables.");
    }

}
