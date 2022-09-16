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

            // Variables
            Material tool = Methods.itemInHand(player).getType();
            UUID uuid = block.getWorld().getUID();
            double amount = plugin.getBlockManager().getAmount(block.getType());

            // Checks
            if(plugin.getBlockManager().getChances().containsKey(block.getType()) && Math.random() * 100 > plugin.getBlockManager().getChance(block.getType())) return;

            // Multipliers
            if(plugin.getDatabaseManager().isCached(player.getUniqueId()) || plugin.getMultiplierManager().hasMultiplierGroup(player.getUniqueId())) {
                MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
                MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

                if(multiplierProfile.getMaterials().containsKey(block.getType())) {
                    amount = amount * multiplierProfile.getMaterials().get(block.getType());
                } else
                if(group != null && group.getMaterials().containsKey(block.getType())){
                    amount = amount * group.getMaterials().get(block.getType());
                }

                if(multiplierProfile.getTools().containsKey(tool)) {
                    amount = amount * multiplierProfile.getTools().get(tool);
                } else
                if(group != null && group.getTools().containsKey(tool)){
                    amount = amount * group.getTools().get(tool);
                }

                if(multiplierProfile.getWorlds().containsKey(uuid)) {
                    amount = amount * multiplierProfile.getWorlds().get(uuid);
                } else
                if(group != null && group.getWorlds().containsKey(uuid)){
                    amount = amount * group.getWorlds().get(uuid);
                }

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
            System.out.println(e);
        }
    }

}
