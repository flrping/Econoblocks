package dev.flrp.econoblocks.placeholder;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.util.multiplier.MultiplierGroup;
import dev.flrp.econoblocks.util.multiplier.MultiplierProfile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EconoblocksExpansion extends PlaceholderExpansion {

    private final Econoblocks plugin;

    public EconoblocksExpansion(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "econoblocks";
    }

    @Override
    public @NotNull String getAuthor() {
        return "flrp";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        if(plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId()) == null) {
            return "1.0";
        }
        String[] params = placeholder.split("_");
        StringBuilder value = new StringBuilder(params[2]);
        MultiplierProfile profile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());
        if(params.length > 3) {
            for(int i = 3; i < params.length; i++) {
                value.append("_").append(params[i]);
            }
        }
        switch (params[1]) {
            case "block":
                return requestBlockPlaceholder(value.toString(), profile, group);
            case "tool":
                return requestToolPlaceholder(value.toString(), profile, group);
            case "world":
                return requestWorldPlaceholder(value.toString(), profile, group);
            default:
                return "";
        }
    }

    private String requestBlockPlaceholder(String value, MultiplierProfile profile, MultiplierGroup group) {
        Material material = null;
        try {
            material = Material.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        if(material != null) {
            if(profile.getMaterials().containsKey(material)) {
                return profile.getMaterials().get(material).toString();
            } else
            if(group != null && group.getMaterials().containsKey(material)) {
                return group.getMaterials().get(material).toString();
            }
        } else {
            if(profile.getCustomMaterials().containsKey(value)) {
                return profile.getCustomMaterials().get(value).toString();
            } else
            if(group != null && group.getCustomMaterials().containsKey(value)) {
                return group.getCustomMaterials().get(value).toString();
            }
        }
        return "1.0";
    }

    private String requestToolPlaceholder(String value, MultiplierProfile profile, MultiplierGroup group) {
        Material material = null;
        try {
            material = Material.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        if(material != null) {
            if(profile.getTools().containsKey(material)) {
                return profile.getTools().get(material).toString();
            } else
            if(group != null && group.getTools().containsKey(material)) {
                return group.getTools().get(material).toString();
            }
        } else {
            if(profile.getCustomTools().containsKey(value)) {
                return profile.getCustomTools().get(value).toString();
            } else
            if(group != null && group.getCustomTools().containsKey(value)) {
                return group.getCustomTools().get(value).toString();
            }
        }
        return "1.0";
    }

    private String requestWorldPlaceholder(String value, MultiplierProfile profile, MultiplierGroup group) {
        if(Bukkit.getWorld(value) != null) {
            UUID world = Bukkit.getWorld(value).getUID();
            if(profile.getWorlds().containsKey(world)) {
                return String.valueOf(profile.getWorlds().get(world));
            } else
            if(group != null && group.getWorlds().containsKey(world)) {
                return String.valueOf(group.getWorlds().get(world));
            }
        }
        return "1.0";
    }



}
