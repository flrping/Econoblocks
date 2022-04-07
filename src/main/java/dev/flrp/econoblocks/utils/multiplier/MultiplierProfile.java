package dev.flrp.econoblocks.utils.multiplier;

import dev.flrp.econoblocks.Econoblocks;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.UUID;

public class MultiplierProfile {

    UUID uuid;
    HashMap<Material, Double> materials = new HashMap<>(), tools = new HashMap<>();
    HashMap<UUID, Double> worlds = new HashMap<>();

    public MultiplierProfile(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public HashMap<Material, Double> getMaterials() {
        return materials;
    }

    public HashMap<Material, Double> getTools() {
        return tools;
    }

    public HashMap<UUID, Double> getWorlds() {
        return worlds;
    }

    public void addBlockMultiplier(Material material, double multiplier) {
        if(materials.containsKey(material)) {
            materials.replace(material, multiplier);
            Econoblocks.getInstance().getDatabaseManager().updateBlockMultiplier(uuid, material, multiplier);
        } else {
            materials.put(material, multiplier);
            Econoblocks.getInstance().getDatabaseManager().addBlockMultiplier(uuid, material, multiplier);
        }
    }

    public void addToolMultiplier(Material material, double multiplier) {
        if(tools.containsKey(material)) {
            tools.replace(material, multiplier);
            Econoblocks.getInstance().getDatabaseManager().updateToolMultiplier(uuid, material, multiplier);
        } else {
            tools.put(material, multiplier);
            Econoblocks.getInstance().getDatabaseManager().addToolMultiplier(uuid, material, multiplier);
        }
    }

    public void addWorldMultiplier(UUID world, double multiplier) {
        if(worlds.containsKey(world)) {
            worlds.replace(world, multiplier);
            Econoblocks.getInstance().getDatabaseManager().updateWorldMultiplier(uuid, world, multiplier);
        } else {
            worlds.put(world, multiplier);
            Econoblocks.getInstance().getDatabaseManager().addWorldMultiplier(uuid, world, multiplier);
        }
    }

    public void removeBlockMultiplier(Material material) {
        materials.remove(material);
        Econoblocks.getInstance().getDatabaseManager().removeBlockMultiplier(uuid, material);
    }

    public void removeToolMultiplier(Material material) {
        tools.remove(material);
        Econoblocks.getInstance().getDatabaseManager().removeToolMultiplier(uuid, material);
    }

    public void removeWorldMultiplier(UUID world) {
        worlds.remove(world);
        Econoblocks.getInstance().getDatabaseManager().removeWorldMultiplier(uuid, world);
    }

}
