package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.api.events.BlockGiveEconomyEvent;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.hooks.VaultHook;
import dev.flrp.econoblocks.utils.Methods;
import dev.flrp.econoblocks.utils.multiplier.MultiplierGroup;
import dev.flrp.econoblocks.utils.multiplier.MultiplierProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyManager {

    private final Econoblocks plugin;

    public EconomyManager(Econoblocks plugin) {
        this.plugin = plugin;
    }

    public void handleDeposit(Player player, Block block) {
        try {
            // Check if player has balance.
            if(!VaultHook.hasAccount(player)) VaultHook.createAccount(player);

            // Multiplier Variables
            Material material = block.getType();
            Material tool = Methods.itemInHand(player).getType();
            UUID uuid = block.getWorld().getUID();

            // Money
            double base = plugin.getBlockManager().getReward(material).calculateReward();

            // Multipliers
            double multiplier = handleMultipliers(player, block, tool, uuid);

            // Mathing
            multiplier = (double) Math.round(multiplier * 100) / 100;
            double result = (double) Math.round((base * multiplier) * 100) / 100;

            // Event
            BlockGiveEconomyEvent blockGiveEconomyEvent = new BlockGiveEconomyEvent(result, block);
            Bukkit.getPluginManager().callEvent(blockGiveEconomyEvent);
            if(blockGiveEconomyEvent.isCancelled()) {
                blockGiveEconomyEvent.setCancelled(true);
                return;
            }

            // Distribution
            if(result == 0) return;
            VaultHook.deposit(player, result);

            // Message
            if(!plugin.getConfig().getBoolean("message.enabled")) return;
            if(plugin.getToggleList().contains(player.getUniqueId())) return;
            plugin.getMessageManager().sendMessage(player, block, base, result, multiplier);

        } catch(Exception e) {
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
            System.out.println(e);
        }
    }

    private double handleMultipliers(Player player, Block block, Material tool, UUID uuid) {
        double multiplier = 1;
        if(plugin.getDatabaseManager().isCached(player.getUniqueId()) || plugin.getMultiplierManager().hasMultiplierGroup(player.getUniqueId())) {
            MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
            MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

            if(multiplierProfile.getMaterials().containsKey(block.getType())) {
                multiplier = multiplier * multiplierProfile.getMaterials().get(block.getType());
            } else
            if(group != null && group.getMaterials().containsKey(block.getType())){
                multiplier = multiplier * group.getMaterials().get(block.getType());
            }

            if(multiplierProfile.getTools().containsKey(tool)) {
                multiplier = multiplier * multiplierProfile.getTools().get(tool);
            } else
            if(group != null && group.getTools().containsKey(tool)){
                multiplier = multiplier * group.getTools().get(tool);
            }

            if(multiplierProfile.getWorlds().containsKey(uuid)) {
                multiplier = multiplier * multiplierProfile.getWorlds().get(uuid);
            } else
            if(group != null && group.getWorlds().containsKey(uuid)){
                multiplier = multiplier * group.getWorlds().get(uuid);
            }
        }
        return multiplier;
    }

}
