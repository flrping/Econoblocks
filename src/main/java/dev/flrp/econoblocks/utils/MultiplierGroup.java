package dev.flrp.econoblocks.utils;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.UUID;

public class MultiplierGroup {

    private final String identifier;
    private final HashMap<Material, Double> materials = new HashMap<>();
    private final HashMap<UUID, Double> worlds = new HashMap<>();

    public MultiplierGroup(String identifier) {
        this.identifier = identifier;
        for(String entry : Econoblocks.getInstance().getConfig().getStringList("multipliers." + identifier + ".tools")) {
            try {
                Material material = Material.getMaterial(entry.substring(0, entry.indexOf(' ')));
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                materials.put(material, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid formatting (" + entry + "), skipping.");
            }
        }
        for(String entry : Econoblocks.getInstance().getConfig().getStringList("multipliers." + identifier + ".worlds")) {
            try {
                UUID uuid = Bukkit.getWorld(entry.substring(0, entry.indexOf(' '))).getUID();
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                worlds.put(uuid, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid formatting (" + entry + "), skipping.");
            } catch (NullPointerException e) {
                Locale.log("&cWorld cannot be found (" + entry + "), skipping.");
            }
        }

    }

    public String getIdentifier() {
        return identifier;
    }

    public HashMap<Material, Double> getMaterials() {
        return materials;
    }

    public HashMap<UUID, Double> getWorlds() {
        return worlds;
    }

}
