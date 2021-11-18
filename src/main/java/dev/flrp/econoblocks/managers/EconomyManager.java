package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.api.events.BlockGiveEconomyEvent;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.hooks.VaultHook;
import dev.flrp.econoblocks.utils.Methods;
import dev.flrp.econoblocks.utils.MultiplierGroup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class EconomyManager {

    private final Econoblocks plugin;
    private final HashMap<String, MultiplierGroup> groups = new HashMap<>();

    public EconomyManager(Econoblocks plugin) {
        this.plugin = plugin;
        // Multipliers
        for(String identifier : plugin.getConfig().getConfigurationSection("multipliers").getKeys(false)) {
            groups.put(identifier, new MultiplierGroup(identifier));
        }
        Locale.log("Loaded &e" + groups.size() + " &rmultiplier groups.");
    }

    public void handleDeposit(Player player, Block block) {
        try {
            // Check if player has balance.
            if(!VaultHook.hasAccount(player)) VaultHook.createAccount(player);

            // Variables
            Material tool = Methods.itemInHand(player).getType();
            UUID uuid = block.getWorld().getUID();
            double amount = plugin.getBlockManager().getAmount(block.getType());

            // Checks
            if(plugin.getBlockManager().getChances().containsKey(block.getType()) && Math.random() * 100 > plugin.getBlockManager().getChance(block.getType())) return;

            // Groups
            String primary = VaultHook.hasGroupSupport() ? VaultHook.getPrimaryGroup(player) : null;
            if(primary != null && groups.containsKey(primary)) {
                MultiplierGroup group = groups.get(primary);
                // Checks
                if(group.getBlocks().containsKey(block.getType())) amount = amount * group.getBlocks().get(block.getType());
                if(group.getMaterials().containsKey(tool)) amount = amount * group.getMaterials().get(tool);
                if(group.getWorlds().containsKey(uuid)) amount = amount * group.getWorlds().get(uuid);
            }

            // Event
            BlockGiveEconomyEvent blockGiveEconomyEvent = new BlockGiveEconomyEvent(amount, block);
            Bukkit.getPluginManager().callEvent(blockGiveEconomyEvent);
            if(blockGiveEconomyEvent.isCancelled()) {
                blockGiveEconomyEvent.setCancelled(true);
                return;
            }

            // Magic
            double dub = (double) Math.round(amount * 100) / 100;
            VaultHook.deposit(player, dub);
            if(plugin.getConfig().getBoolean("message.enabled") && !plugin.getToggleList().contains(player))
                plugin.getMessageManager().sendMessage(player, block, dub);
        } catch(Exception e) {
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
        }
    }

}
