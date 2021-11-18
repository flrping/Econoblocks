package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockManager {

    List<String> worlds;
    HashMap<Material, Double> amounts = new HashMap<>();
    HashMap<Material, Double> chances = new HashMap<>();

    public BlockManager(Econoblocks plugin) {
        for(Map.Entry<String, Object> entry : plugin.getBlocks().getConfiguration().getConfigurationSection("blocks").getValues(false).entrySet()) {
            Material material = Material.matchMaterial(entry.getKey());
            if(material == null) {
                Locale.log("&cInvalid material found (" + entry.getKey() +"), skipping.");
                continue;
            }
            String value = String.valueOf(entry.getValue());
            amounts.put(material, value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value));
            chances.put(material, value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100);
        }
        worlds = plugin.getConfig().getStringList("world-blacklist");
    }

    public List<String> getBlacklistedWorlds() {
        return worlds;
    }

    public double getAmount(Material material) {
        return amounts.get(material);
    }

    public double getChance(Material material) {
        return chances.get(material);
    }

    public HashMap<Material, Double> getAmounts() {
        return amounts;
    }

    public HashMap<Material, Double> getChances() {
        return chances;
    }
}
