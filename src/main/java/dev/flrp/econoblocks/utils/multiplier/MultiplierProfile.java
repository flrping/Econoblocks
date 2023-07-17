package dev.flrp.econoblocks.utils.multiplier;

import dev.flrp.econoblocks.Econoblocks;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.UUID;

public class MultiplierProfile {

    UUID uuid;
    HashMap<Material, Double> materials = new HashMap<>(), tools = new HashMap<>();
    HashMap<UUID, Double> worlds = new HashMap<>();
    HashMap<String, Double> customMaterials = new HashMap<>(), customTools = new HashMap<>();

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

    public HashMap<String, Double> getCustomMaterials() {
        return customMaterials;
    }

    public HashMap<String, Double> getCustomTools() {
        return customTools;
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

    public void addCustomBlockMultiplier(String material, double multiplier) {
        if(customMaterials.containsKey(material)) {
            customMaterials.replace(material, multiplier);
            Econoblocks.getInstance().getDatabaseManager().updateCustomBlockMultiplier(uuid, material, multiplier);
        } else {
            customMaterials.put(material, multiplier);
            Econoblocks.getInstance().getDatabaseManager().addCustomBlockMultiplier(uuid, material, multiplier);
        }
    }

    public void addCustomToolMultiplier(String material, double multiplier) {
        if(customTools.containsKey(material)) {
            customTools.replace(material, multiplier);
            Econoblocks.getInstance().getDatabaseManager().updateCustomToolMultiplier(uuid, material, multiplier);
        } else {
            customTools.put(material, multiplier);
            Econoblocks.getInstance().getDatabaseManager().addCustomToolMultiplier(uuid, material, multiplier);
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

    public void removeCustomBlockMultiplier(String material) {
        customMaterials.remove(material);
        Econoblocks.getInstance().getDatabaseManager().removeCustomBlockMultiplier(uuid, material);
    }

    public void removeCustomToolMultiplier(String material) {
        customTools.remove(material);
        Econoblocks.getInstance().getDatabaseManager().removeCustomToolMultiplier(uuid, material);
    }

}
