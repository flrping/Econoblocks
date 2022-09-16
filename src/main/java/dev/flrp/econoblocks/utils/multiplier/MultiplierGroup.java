package dev.flrp.econoblocks.utils.multiplier;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.UUID;

public class MultiplierGroup {

    private final String identifier;
    private final int weight;
    private final HashMap<Material, Double> tools = new HashMap<>();
    private final HashMap<UUID, Double> worlds = new HashMap<>();
    private final HashMap<Material, Double> materials = new HashMap<>();

    public MultiplierGroup(String identifier) {
        this.identifier = identifier;
        weight = Econoblocks.getInstance().getConfig().contains("multipliers." + identifier + ".weight") ? Econoblocks.getInstance().getConfig().getInt("multipliers." + identifier + ".weight") : 0;
        for(String entry : Econoblocks.getInstance().getConfig().getStringList("multipliers." + identifier + ".blocks")) {
            try {
                Material material = Material.getMaterial(entry.substring(0, entry.indexOf(' ')));
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                materials.put(material, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid entry (" + entry +"), skipping.");
            }
        }
        for(String entry : Econoblocks.getInstance().getConfig().getStringList("multipliers." + identifier + ".tools")) {
            try {
                Material material = Material.getMaterial(entry.substring(0, entry.indexOf(' ')));
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                tools.put(material, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid entry (" + entry + "), skipping.");
            }
        }
        for(String entry : Econoblocks.getInstance().getConfig().getStringList("multipliers." + identifier + ".worlds")) {
            try {
                UUID uuid = Bukkit.getWorld(entry.substring(0, entry.indexOf(' '))).getUID();
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                worlds.put(uuid, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid entry (" + entry + "), skipping.");
            } catch (NullPointerException e) {
                Locale.log("&cWorld cannot be found (" + entry + "), skipping.");
            }
        }

    }

    /*
     * Returns the identifier for the group.
     */
    public String getIdentifier() {
        return identifier;
    }

    /*
     * Returns the weight of the group.
     */
    public int getWeight() {
        return weight;
    }

    /*
     * Returns the list used for block multipliers.
     */
    public HashMap<Material, Double> getMaterials() {
        return materials;
    }

    /*
     * Returns the list used for tool multipliers.
     */
    public HashMap<Material, Double> getTools() {
        return tools;
    }

    /*
     * Returns the list used for world multipliers.
     */
    public HashMap<UUID, Double> getWorlds() {
        return worlds;
    }

}
