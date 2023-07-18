package dev.flrp.econoblocks.configuration;

import dev.flrp.econoblocks.Econoblocks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Configuration {

    private final Econoblocks plugin;

    public FileConfiguration fileConfig;
    public File file;
    public String path;

    public Configuration(Econoblocks plugin) {
        this.plugin = plugin;
    }

    public void load(String path) {
        this.file = new File(plugin.getDataFolder(), path + ".yml");
        if(!file.exists()) {
            if(plugin.getResource(path + ".yml") != null) {
                plugin.saveResource(path + ".yml", false);
            } else {
                try {
                    file.getParentFile().mkdir();
                    file.createNewFile();
                } catch (IOException e) {
                    Locale.log("&cFailed to create " + path + ".yml");
                }
            }
        }
        this.path = path;
        this.fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            Locale.log("&cfailed to save " + path + ".yml");
        }
    }

    public FileConfiguration getConfiguration() {
        return this.fileConfig;
    }

}
