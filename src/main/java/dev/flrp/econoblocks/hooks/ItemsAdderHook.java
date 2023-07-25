package dev.flrp.econoblocks.hooks;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Configuration;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.listeners.ItemsAdderListeners;
import dev.flrp.econoblocks.utils.block.Reward;
import dev.flrp.econoblocks.utils.multiplier.MultiplierGroup;
import dev.lone.itemsadder.api.CustomStack;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class ItemsAdderHook implements Listener {

    private static final Econoblocks instance = Econoblocks.getInstance();

    private static final HashMap<String, Reward> itemsAdderBlocks = new HashMap<>();

    public static void register() {
        if(!isEnabled()) return;
        Locale.log("&eItemsAdder&r found. Attempting to hook.");
        build();
        Bukkit.getPluginManager().registerEvents(new ItemsAdderListeners(instance), instance);
    }

    public static void reload() {
        if(!isEnabled()) return;
        itemsAdderBlocks.clear();
        build();
    }

    public static void build() {
        Configuration itemsAdderFile = new Configuration(instance);
        itemsAdderFile.load("hooks/ItemsAdder");

        // Initial build
        // Builds both the mob list and the multiplier groups.
        if(itemsAdderFile.getConfiguration().getConfigurationSection("multipliers") == null) {
            itemsAdderFile.getConfiguration().set("multipliers.example.blocks", new ArrayList<>(Collections.singletonList("carved_wood_block 1.2")));
            itemsAdderFile.getConfiguration().set("multipliers.example.tools", new ArrayList<>(Collections.singletonList("emerald_pickaxe 1.2")));
            itemsAdderFile.save();
        }
        if(itemsAdderFile.getConfiguration().getConfigurationSection("blocks") == null) {
            itemsAdderFile.getConfiguration().createSection("blocks");
            itemsAdderFile.getConfiguration().set("blocks.carved_wood_block", new ArrayList<>(Collections.singletonList("10")));
            itemsAdderFile.save();
        }

        // Reward creation for ItemsAdder blocks.
        Set<String> blockSet = itemsAdderFile.getConfiguration().getConfigurationSection("blocks").getKeys(false);
        for(String block : blockSet) {
            Reward reward = new Reward();

            for(String value : itemsAdderFile.getConfiguration().getStringList("blocks." + block)) {
                double amount = value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value);
                double chance = value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100;
                reward.getDropList().put(amount, chance);
                reward.setTotal(reward.getTotal() + chance);
            }
            itemsAdderBlocks.put(block, reward);
        }

        // Adding multipliers to groups
        Set<String> multiplierSet = itemsAdderFile.getConfiguration().getConfigurationSection("multipliers").getKeys(false);
        // Loop through all multipliers "groups" present in the ItemsAdder file.
        for (String multiplier : multiplierSet) {

            // Configuring an existing or new group.
            // If the group already exists, we'll just get it.
            // If it doesn't, we'll create it and add it to the manager. This allows groups to be purely for ItemsAdder blocks/items.
            MultiplierGroup multiplierGroup;
            if(instance.getMultiplierManager().isMultiplierGroup(multiplier)) {
                multiplierGroup = instance.getMultiplierManager().getMultiplierGroup(multiplier);
            } else {
                multiplierGroup = new MultiplierGroup(multiplier);
                int weight = itemsAdderFile.getConfiguration().contains("multipliers." + multiplier + ".weight") ? itemsAdderFile.getConfiguration().getInt("multipliers." + multiplier + ".weight") : 0;
                multiplierGroup.setWeight(weight);
                instance.getMultiplierManager().addMultiplierGroup(multiplier, multiplierGroup);
            }

            // Getting both material and tool multipliers and adding to group.
            for(String entry : itemsAdderFile.getConfiguration().getStringList("multipliers." + multiplier + ".blocks")) {
                try {
                    String material = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomMaterialMultiplier(material, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }
            for(String entry : itemsAdderFile.getConfiguration().getStringList("multipliers." + multiplier + ".tools")) {
                try {
                    String material = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomToolMultiplier(material, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }

        }

        Locale.log("Loaded &e" + multiplierSet.size() + " &rItemsAdder multipliers.");
        Locale.log("Loaded &e" + itemsAdderBlocks.size() + " &rItemsAdder blocks.");

    }

    // Methods
    public static boolean isEnabled() {
        if(!instance.getConfig().getBoolean("hooks.ItemsAdder")) return false;
        return Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
    }

    public static boolean isCustomStack(ItemStack itemStack) {
        if (!isEnabled()) return false;
        CustomStack stack = CustomStack.byItemStack(itemStack);
        return stack != null;
    }

    public static String getCustomItem(ItemStack itemStack) {
        CustomStack stack = CustomStack.byItemStack(itemStack);
        return stack != null ? getName(stack.getNamespacedID()) : null;
    }

    public static HashMap<String, Reward> getRewards() {
        return itemsAdderBlocks;
    }

    public static Reward getReward(String blockID) {
        return itemsAdderBlocks.get(blockID);
    }

    public static boolean hasReward(String blockID) {
        return itemsAdderBlocks.containsKey(blockID);
    }

    public static String getName(String input) {
        int colonIndex = input.indexOf(":");
        if (colonIndex != -1) {
            input = input.substring(colonIndex + 1); // Remove everything up to the first ":"
        }

        if (input.endsWith("_z") || input.endsWith("_y") || input.endsWith("_x")) {
            input = input.substring(0, input.length() - 2); // Remove the ending if it has "_z", "_y", or "_x"
        }

        return input;
    }
    
}
