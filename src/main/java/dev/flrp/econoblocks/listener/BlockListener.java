package dev.flrp.econoblocks.listener;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.espresso.hook.block.BlockProvider;
import dev.flrp.espresso.table.LootContainer;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockListener implements Listener {

    private final Econoblocks plugin;

    public BlockListener(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if(!plugin.getConfig().getBoolean("gamemode.creative-rewards") && player.getGameMode() == GameMode.CREATIVE) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        if(!plugin.getHookManager().getBlockProviders().isEmpty()) {
            for(BlockProvider blockProvider : plugin.getHookManager().getBlockProviders()) if(blockProvider.isCustomBlock(block)) return;
        }
        if(!plugin.getRewardManager().hasLootContainer(block.getType())) {
            if(plugin.getRewardManager().getDefaultLootContainer().getLootTables().isEmpty()) return;
            if(plugin.getRewardManager().getExcludedMaterials().contains(block.getType())) return;
        }
        if(plugin.getDatabaseManager().isCached(block.getLocation())) {
            plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
            return;
        }
        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(block.getType())
                ? plugin.getRewardManager().getLootContainer(block.getType()) : plugin.getRewardManager().getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(player, block, lootContainer, block.getType().name());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        if(!plugin.getHookManager().getBlockProviders().isEmpty()) {
            for(BlockProvider blockProvider : plugin.getHookManager().getBlockProviders()) if(blockProvider.isCustomBlock(block)) return;
        }
        if(!plugin.getRewardManager().hasLootContainer(block.getType())) {
            if(plugin.getRewardManager().getDefaultLootContainer().getLootTables().isEmpty()) return;
            if(plugin.getRewardManager().getExcludedMaterials().contains(block.getType())) return;
        }
        plugin.getDatabaseManager().addBlockEntry(block.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for(Block block : event.blockList()) {
            if(plugin.getDatabaseManager().isCached(block.getLocation())) {
                plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for(Block block : event.getBlocks()) {
            if(plugin.getDatabaseManager().isCached(block.getLocation())) {
                plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
                plugin.getDatabaseManager().addBlockEntry(block.getLocation().add(event.getDirection().getDirection()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for(Block block : event.getBlocks()) {
            if(plugin.getDatabaseManager().isCached(block.getLocation())) {
                plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
                plugin.getDatabaseManager().addBlockEntry(block.getLocation().add(event.getDirection().getDirection()));
            }
        }
    }
}
