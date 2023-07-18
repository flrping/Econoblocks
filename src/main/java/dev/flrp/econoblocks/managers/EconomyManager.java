package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.api.events.BlockGiveEconomyEvent;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.hooks.ItemsAdderHook;
import dev.flrp.econoblocks.hooks.VaultHook;
import dev.flrp.econoblocks.utils.Methods;
import dev.flrp.econoblocks.utils.multiplier.MultiplierGroup;
import dev.flrp.econoblocks.utils.multiplier.MultiplierProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EconomyManager {

    private final Econoblocks plugin;

    public EconomyManager(Econoblocks plugin) {
        this.plugin = plugin;
    }

    public void handleCustomBlockDeposit(Player player, Block block, String blockName) {
        // Check if player has balance.
        if(!VaultHook.hasAccount(player)) VaultHook.createAccount(player);

        // Multiplier Variables
        ItemStack itemStack = Methods.itemInHand(player);
        Material tool = itemStack.getType();

        UUID uuid = block.getWorld().getUID();

        // Custom Checks
        String toolName;
        if(ItemsAdderHook.isCustomStack(itemStack)) {
            toolName = ItemsAdderHook.getCustomItem(itemStack);
        } else toolName = tool.name();

        double base = ItemsAdderHook.getReward(blockName).calculateReward();

        // Multipliers
        double multiplier = handleMultipliers(player, blockName, toolName, uuid);

        // Math
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
    }


    public void handleDeposit(Player player, Block block) {
        try {
            // Check if player has balance.
            if(!VaultHook.hasAccount(player)) VaultHook.createAccount(player);

            // Multiplier Variables
            Material material = block.getType();

            ItemStack itemStack = Methods.itemInHand(player);
            Material tool = itemStack.getType();

            UUID uuid = block.getWorld().getUID();

            // Custom Checks
            String toolName;
            if(ItemsAdderHook.isCustomStack(itemStack)) {
                toolName = ItemsAdderHook.getCustomItem(itemStack);
            } else toolName = tool.name();

            // Money
            double base = plugin.getBlockManager().getReward(material).calculateReward();

            // Multipliers
            double multiplier = handleMultipliers(player, block.getType().name(), toolName, uuid);

            // Math
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

    public double handleMultipliers(Player player, String material, String tool, UUID uuid) {
        double multiplier = 1;
        if(plugin.getDatabaseManager().isCached(player.getUniqueId()) || plugin.getMultiplierManager().hasMultiplierGroup(player.getUniqueId())) {
            MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
            MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

            // Profile Multipliers
            //  - Vanilla Multipliers
            //  - Custom Multipliers
            // Group Multipliers
            //  - Vanilla Multipliers
            //  - Custom Multipliers

            if(Material.matchMaterial(material) != null) {
                if(multiplierProfile.getMaterials().containsKey(Material.matchMaterial(material))) {
                    multiplier = multiplier * multiplierProfile.getMaterials().get(Material.matchMaterial(material));
                } else
                if(group != null && group.getMaterials().containsKey(Material.matchMaterial(material))) {
                    multiplier = multiplier * group.getMaterials().get(Material.matchMaterial(material));
                }
            } else {
                if(multiplierProfile.getCustomMaterials().containsKey(material)) {
                    multiplier = multiplier * multiplierProfile.getCustomMaterials().get(material);
                } else
                if(group != null && group.getCustomMaterials().containsKey(material)) {
                    multiplier = multiplier * group.getCustomMaterials().get(material);
                }
            }

            if(Material.matchMaterial(tool) != null) {
                if(multiplierProfile.getTools().containsKey(Material.matchMaterial(tool))) {
                    multiplier = multiplier * multiplierProfile.getTools().get(Material.matchMaterial(tool));
                } else
                if(group != null && group.getTools().containsKey(Material.matchMaterial(tool))) {
                    multiplier = multiplier * group.getTools().get(Material.matchMaterial(tool));
                }
            } else {
                if(multiplierProfile.getCustomTools().containsKey(tool)) {
                    multiplier = multiplier * multiplierProfile.getCustomTools().get(tool);
                } else
                if(group != null && group.getCustomTools().containsKey(tool)) {
                    multiplier = multiplier * group.getCustomTools().get(tool);
                }
            }

            if(multiplierProfile.getWorlds().containsKey(uuid)) {
                multiplier = multiplier * multiplierProfile.getWorlds().get(uuid);
            }

        }

        return multiplier;
    }

}
