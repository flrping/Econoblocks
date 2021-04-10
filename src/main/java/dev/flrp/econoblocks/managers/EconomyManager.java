package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.api.events.BlockGiveEconomyEvent;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class EconomyManager {

    private final Econoblocks plugin;

    private static Economy eco = null;
    private final HashMap<Material, Double> tools = new HashMap<>();
    private final HashMap<World, Double> worlds = new HashMap<>();

    public EconomyManager(Econoblocks plugin) {
        this.plugin = plugin;
        if(plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            eco = rsp.getProvider();
        }
        for(String entry : plugin.getConfig().getStringList("multipliers.tools")) {
            Material material = Material.getMaterial(entry.substring(0, entry.indexOf(' ')));
            double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
            tools.put(material, multiplier);
        }
        for(String entry : plugin.getConfig().getStringList("multipliers.worlds")) {
            World world = Bukkit.getWorld(entry.substring(0, entry.indexOf(' ')));
            double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
            worlds.put(world, multiplier);
        }
    }

    public void handleDeposit(Player player, Block block) {
        try {
            if(hasAccount(player)) {
                // Variables
                Material tool = Methods.itemInHand(player).getType();
                double value = plugin.getBlockManager().getAmount(block.getType());
                World world = block.getWorld();

                // Checks
                if(plugin.getBlockManager().getChances().containsKey(block.getType()) && Math.random() * 100 > plugin.getBlockManager().getChance(block.getType())) return;
                if(tools.containsKey(tool)) value = value * tools.get(tool);
                if(worlds.containsKey(world)) value = value * worlds.get(world);

                // Event
                BlockGiveEconomyEvent blockGiveEconomyEvent = new BlockGiveEconomyEvent(value, block);
                Bukkit.getPluginManager().callEvent(blockGiveEconomyEvent);
                if(blockGiveEconomyEvent.isCancelled()) {
                    blockGiveEconomyEvent.setCancelled(true);
                    return;
                }

                // Magic
                String str = String.valueOf(BigDecimal.valueOf(value).setScale(2, RoundingMode.DOWN));
                deposit(player, NumberUtils.toDouble(str));
                player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_GIVEN.replace("{0}", str)));
                return;
            }
            // Couldn't apply money.
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_FAILED));
        } catch(Exception e) {
            // This won't always be the reason, but most of the time it will be.
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
        }
    }

    public boolean hasAccount(OfflinePlayer player) {
        return eco.hasAccount(player);
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        return eco.depositPlayer(player, amount).transactionSuccess();
    }

    public HashMap<Material, Double> getToolMultipliers() { return tools; }

    public HashMap<World, Double> getWorldMultipliers() { return worlds; }

}
