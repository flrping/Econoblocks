package dev.flrp.econoblocks.configuration;

import dev.flrp.econoblocks.Econoblocks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Configuration {

    private Econoblocks plugin;

    public FileConfiguration fileConfig;
    public File file;
    public String name;

    public Configuration(Econoblocks plugin) {
        this.plugin = plugin;
    }

    public void load(String name) {
        this.file = new File(plugin.getDataFolder(), name + ".yml");
        if(!file.exists()) {
            plugin.saveResource(name + ".yml", false);
        }
        this.name = name;
        this.fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            System.out.print("[Econoblocks] failed to save " + name + ".yml");
        }
    }

    public FileConfiguration getConfiguration() {
        return this.fileConfig;
    }

}
