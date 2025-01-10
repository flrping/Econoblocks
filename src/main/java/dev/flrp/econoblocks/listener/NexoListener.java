package dev.flrp.econoblocks.listener;

import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockBreakEvent;
import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.hook.block.NexoBlockHook;
import dev.flrp.espresso.hook.block.BlockType;
import dev.flrp.espresso.table.LootContainer;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NexoListener implements Listener {

    private final Econoblocks plugin;

    public NexoListener(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void nexoNoteBlockBreak(NexoNoteBlockBreakEvent event) {
        breakEvent(event.getPlayer(), event.getBlock(), event.getMechanic().getItemID());
    }

    @EventHandler
    public void nexoStringBlockBreak(NexoStringBlockBreakEvent event) {
        breakEvent(event.getPlayer(), event.getBlock(), event.getMechanic().getItemID());
    }

    @EventHandler
    public void nexoNoteBlockPlace(NexoNoteBlockBreakEvent event) {
        placeEvent(event.getBlock(), event.getMechanic().getItemID());
    }

    @EventHandler
    public void nexoStringBlockPlace(NexoStringBlockBreakEvent event) {
        placeEvent(event.getBlock(), event.getMechanic().getItemID());
    }

    private void breakEvent(Player player, Block block, String blockName) {
        if(!plugin.getConfig().getBoolean("gamemode.creative-rewards") && player.getGameMode() == GameMode.CREATIVE) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        NexoBlockHook nexoHook = (NexoBlockHook) plugin.getHookManager().getBlockProvider(BlockType.NEXO);
        if(!nexoHook.hasLootContainer(blockName)) {
            if(nexoHook.getExcludedMaterials().contains(blockName)) return;
        }
        if(plugin.getDatabaseManager().isCached(block.getLocation())) {
            plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
            return;
        }
        LootContainer lootContainer = nexoHook.hasLootContainer(blockName)
                ? nexoHook.getLootContainer(blockName) : nexoHook.getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(player, block, lootContainer, blockName);
    }


    private void placeEvent(Block block, String blockName) {
        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        NexoBlockHook nexoHook = (NexoBlockHook) plugin.getHookManager().getBlockProvider(BlockType.NEXO);
        if(!nexoHook.hasLootContainer(blockName)) {
            if(nexoHook.getExcludedMaterials().contains(blockName)) return;
        }
        plugin.getDatabaseManager().addBlockEntry(block.getLocation());
    }

}
