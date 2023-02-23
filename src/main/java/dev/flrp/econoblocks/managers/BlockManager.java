package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.utils.block.Reward;
import org.bukkit.Material;

import java.util.*;

public class BlockManager {

    private final List<String> worlds;
    private final HashMap<Material, Reward> rewards = new HashMap<>();

    public BlockManager(Econoblocks plugin) {

        // Reward Creation
        Set<String> blockSet = plugin.getBlocks().getConfiguration().getConfigurationSection("blocks").getKeys(false);

        for(String block : blockSet) {
            Material material = Material.matchMaterial(block);

            if(material == null) {
                Locale.log("&cInvalid material found (" + block +"), skipping.");
                continue;
            }
            Reward reward = new Reward();

            // Conversion - TEMPORARY
            if(plugin.getBlocks().getConfiguration().getStringList("blocks." + block).isEmpty()) {
                Locale.log(block + " is using the old format. Attempting to convert...");
                try {
                    String oldValue = plugin.getBlocks().getConfiguration().getString("blocks." + block);
                    plugin.getBlocks().getConfiguration().set("blocks." + block, new ArrayList<>(Collections.singletonList(oldValue)));
                    plugin.getBlocks().save();
                } catch (Exception e) {
                    Locale.log("Could not convert " + block + " configuration section.");
                }
            }

            for (String value : plugin.getBlocks().getConfiguration().getStringList("blocks." + block)){
                double amount = value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value);
                double chance = value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100;
                reward.getDropList().put(amount, chance);
                reward.setTotal(reward.getTotal() + chance);
            }
            rewards.put(material, reward);
        }

        Locale.log("Loaded &e" + rewards.size() + " &rrewards.");
        worlds = plugin.getConfig().getStringList("world-blacklist");
    }

    public List<String> getBlacklistedWorlds() {
        return worlds;
    }

    public HashMap<Material, Reward> getRewards() {
        return rewards;
    }

    public Reward getReward(Material material) {
        return rewards.get(material);
    }

    public boolean hasReward(Material material) {
        return rewards.containsKey(material);
    }

}
