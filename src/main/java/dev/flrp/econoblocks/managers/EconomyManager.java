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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.UUID;

public class EconomyManager {

    private final Econoblocks plugin;

    private static Economy eco = null;
    private final HashMap<Material, Double> tools = new HashMap<>();
    private final HashMap<UUID, Double> worlds = new HashMap<>();

    public EconomyManager(Econoblocks plugin) {
        this.plugin = plugin;
        if(plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            eco = rsp.getProvider();
        }
        for(String entry : plugin.getConfig().getStringList("multipliers.tools")) {
            try {
                Material material = Material.getMaterial(entry.substring(0, entry.indexOf(' ')));
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                tools.put(material, multiplier);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("[Econoblocks] Invalid formatting (" + entry + "), skipping.");
            }
        }
        for(String entry : plugin.getConfig().getStringList("multipliers.worlds")) {
            try {
                UUID uuid = Bukkit.getWorld(entry.substring(0, entry.indexOf(' '))).getUID();
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                worlds.put(uuid, multiplier);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("[Econoblocks] Invalid formatting (" + entry + "), skipping.");
            } catch (NullPointerException e) {
                System.out.println("[Econoblocks] World cannot be found (" + entry + "), skipping.");
            }
        }
    }

    public void handleDeposit(Player player, Block block) {
        try {
            if(!eco.hasAccount(player)) eco.createPlayerAccount(player);

            // Variables
            Material tool = Methods.itemInHand(player).getType();
            UUID uuid = block.getWorld().getUID();
            double value = plugin.getBlockManager().getAmount(block.getType());

            // Checks
            if(plugin.getBlockManager().getChances().containsKey(block.getType()) && Math.random() * 100 > plugin.getBlockManager().getChance(block.getType())) return;
            if(tools.containsKey(tool)) value = value * tools.get(tool);
            if(worlds.containsKey(uuid)) value = value * worlds.get(uuid);

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

            // Message
            if(plugin.getConfig().getBoolean("message.enabled") && !plugin.getToggleList().contains(player))
                plugin.getMessageManager().sendMessage(player, block, NumberUtils.toDouble(str));
        } catch(Exception e) {
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
        }
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        return eco.depositPlayer(player, amount).transactionSuccess();
    }

    public HashMap<Material, Double> getToolMultipliers() { return tools; }

    public HashMap<UUID, Double> getWorldMultipliers() { return worlds; }

}
