package dev.flrp.econoblocks.listener;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.hook.block.OraxenBlockHook;
import dev.flrp.espresso.hook.block.BlockType;
import dev.flrp.espresso.table.LootContainer;
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockBreakEvent;
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockPlaceEvent;
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockBreakEvent;
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockPlaceEvent;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OraxenListener implements Listener {

    private final Econoblocks plugin;

    public OraxenListener(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void oraxenNoteBlockBreak(OraxenNoteBlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = event.getMechanic().getItemID();

        if(!plugin.getConfig().getBoolean("gamemode.creative-rewards") && player.getGameMode() == GameMode.CREATIVE) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        OraxenBlockHook oraxenHook = (OraxenBlockHook) plugin.getHookManager().getBlockProvider(BlockType.ORAXEN);
        if(!oraxenHook.hasLootContainer(blockName)) {
            if(oraxenHook.getExcludedMaterials().contains(blockName)) return;
        }
        if(plugin.getDatabaseManager().isCached(block.getLocation())) {
            plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
            return;
        }
        LootContainer lootContainer = oraxenHook.hasLootContainer(blockName)
                ? oraxenHook.getLootContainer(blockName) : oraxenHook.getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(player, block, lootContainer, blockName);
    }

    @EventHandler
    public void oraxenStringBlockBreak(OraxenStringBlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = event.getMechanic().getItemID();

        if(!plugin.getConfig().getBoolean("gamemode.creative-rewards") && player.getGameMode() == GameMode.CREATIVE) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        OraxenBlockHook oraxenHook = (OraxenBlockHook) plugin.getHookManager().getBlockProvider(BlockType.ORAXEN);
        if(!oraxenHook.hasLootContainer(blockName)) {
            if(oraxenHook.getExcludedMaterials().contains(blockName)) return;
        }
        if(plugin.getDatabaseManager().isCached(block.getLocation())) {
            plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
            return;
        }
        LootContainer lootContainer = oraxenHook.hasLootContainer(blockName)
                ? oraxenHook.getLootContainer(blockName) : oraxenHook.getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(player, block, lootContainer, blockName);

    }

    @EventHandler
    public void oraxenNoteBlockPlace(OraxenNoteBlockPlaceEvent event) {
        Block block = event.getBlock();
        String blockName = event.getMechanic().getItemID();

        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        OraxenBlockHook oraxenHook = (OraxenBlockHook) plugin.getHookManager().getBlockProvider(BlockType.ORAXEN);
        if(!oraxenHook.hasLootContainer(blockName)) {
            if(oraxenHook.getDefaultLootContainer().getLootTables().isEmpty()) return;
            if(oraxenHook.getExcludedMaterials().contains(blockName)) return;
        }
        plugin.getDatabaseManager().addBlockEntry(block.getLocation());
    }

    @EventHandler
    public void oraxenStringBlockPlace(OraxenStringBlockPlaceEvent event) {
        Block block = event.getBlock();
        String blockName = event.getMechanic().getItemID();

        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        OraxenBlockHook oraxenHook = (OraxenBlockHook) plugin.getHookManager().getBlockProvider(BlockType.ORAXEN);
        if(!oraxenHook.hasLootContainer(blockName)) {
            if(oraxenHook.getExcludedMaterials().contains(blockName)) return;
        }
        plugin.getDatabaseManager().addBlockEntry(block.getLocation());
    }

}
