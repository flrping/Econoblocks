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

    private String requestToolPlaceholder(String subject, MultiplierProfile profile, MultiplierGroup group) {
        Material material = null;
        try {
            material = Material.valueOf(subject.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        if(material != null) {
            if(profile.getTools().containsKey(material)) {
                return profile.getTools().get(material).toString();
            } else
            if(group != null && group.getTools().containsKey(material)) {
                return group.getTools().get(material).toString();
            }
        } else {
            if(profile.getCustomTools().containsKey(subject)) {
                return profile.getCustomTools().get(subject).toString();
            } else
            if(group != null && group.getCustomTools().containsKey(subject)) {
                return group.getCustomTools().get(subject).toString();
            }
        }
        return "1.0";
    }

    private String requestWorldPlaceholder(String subject, MultiplierProfile profile, MultiplierGroup group) {
        try {
            if(Bukkit.getWorld(UUID.fromString(subject)) != null) {
                if(profile.getWorlds().containsKey(UUID.fromString(subject))) {
                    return profile.getWorlds().get(UUID.fromString(subject)).toString();
                } else
                if(group != null && group.getWorlds().containsKey(UUID.fromString(subject))) {
                    return group.getWorlds().get(UUID.fromString(subject)).toString();
                }
            }
        } catch (IllegalArgumentException ignored) {}
        return "1.0";
    }



}
