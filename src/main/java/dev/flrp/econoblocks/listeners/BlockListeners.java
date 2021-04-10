package dev.flrp.econoblocks.listeners;

import dev.flrp.econoblocks.Econoblocks;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListeners implements Listener {

    private final Econoblocks plugin;

    public BlockListeners(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if(plugin.getBlockManager().getBlacklistedWorlds().contains(block.getWorld().getName())) return;
        if(!plugin.getBlockManager().getAmounts().containsKey(block.getType())) return;
        plugin.getEconomyManager().handleDeposit(player, block);
    }

}
