package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.utils.multiplier.MultiplierGroup;
import dev.flrp.econoblocks.utils.multiplier.MultiplierProfile;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class MultiplierManager {

    Econoblocks plugin;
    private final HashMap<String, MultiplierGroup> groups = new HashMap<>();

    public MultiplierManager(Econoblocks plugin) {
        this.plugin = plugin;
        for(String identifier : plugin.getConfig().getConfigurationSection("multipliers").getKeys(false)) {
            groups.put(identifier, new MultiplierGroup(identifier));
        }
        Locale.log("Loaded &e" + groups.size() + " &rmultiplier groups.");
    }

    public MultiplierProfile getMultiplierProfile(UUID uuid) {
        return plugin.getDatabaseManager().getMultiplierProfile(uuid);
    }

    public MultiplierGroup getMultiplierGroup(String identifier) {
        return groups.get(identifier);
    }

    public MultiplierGroup getMultiplierGroup(UUID uuid) {
        Set<PermissionAttachmentInfo> infoSet = Bukkit.getPlayer(uuid).getEffectivePermissions();
        for(PermissionAttachmentInfo info : infoSet) {
            if(!info.getPermission().startsWith("econoblocks.group.")) continue;
            return groups.get(info.getPermission().substring(18));
        }
        return null;
    }

    public boolean isMultiplierGroup(String identifier) {
        return groups.containsKey(identifier);
    }

}
