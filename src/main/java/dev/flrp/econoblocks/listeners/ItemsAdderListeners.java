package dev.flrp.econoblocks.listeners;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.hooks.ItemsAdderHook;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderListeners implements Listener {

    private final Econoblocks plugin;

    public ItemsAdderListeners(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void itemsAdderLoad(ItemsAdderLoadDataEvent event) {

    }

    @EventHandler
    public void itemsAdderBlockBreak(CustomBlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockID = event.getNamespacedID();

        if(!plugin.getConfig().getBoolean("gamemode.creative-rewards") && player.getGameMode() == GameMode.CREATIVE) return;
        if(plugin.getBlockManager().getBlacklistedWorlds().contains(block.getWorld().getName())) return;
        if(!ItemsAdderHook.hasReward(ItemsAdderHook.getName(blockID))) return;
        if(plugin.getDatabaseManager().isCached(block.getLocation())) {
            plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
            return;
        }
        plugin.getEconomyManager().handleCustomBlockDeposit(player, block, ItemsAdderHook.getName(blockID));
    }

    @EventHandler
    public void itemsAdderBlockPlace(CustomBlockPlaceEvent event) {
        Block block = event.getBlock();
        String blockID = event.getNamespacedID();

        if(plugin.getBlockManager().getBlacklistedWorlds().contains(block.getWorld().getName())) return;
        if(!ItemsAdderHook.hasReward(ItemsAdderHook.getName(blockID))) return;
        plugin.getDatabaseManager().addBlockEntry(block.getLocation());
    }

}
