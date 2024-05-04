package dev.flrp.econoblocks.listener;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.hook.block.ItemsAdderBlockHook;
import dev.flrp.espresso.hook.block.BlockType;
import dev.flrp.espresso.table.LootContainer;
import dev.flrp.espresso.util.StringUtils;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderListener implements Listener {

    private final Econoblocks plugin;

    public ItemsAdderListener(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void itemsAdderBlockBreak(CustomBlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = StringUtils.getItemsAdderName(event.getNamespacedID());

        if(!plugin.getConfig().getBoolean("gamemode.creative-rewards") && player.getGameMode() == GameMode.CREATIVE) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        ItemsAdderBlockHook itemsAdderHook = (ItemsAdderBlockHook) plugin.getHookManager().getBlockProvider(BlockType.ITEMS_ADDER);
        if(!itemsAdderHook.hasLootContainer(blockName)) {
            if(itemsAdderHook.getExcludedMaterials().contains(blockName)) return;
        }
        if(plugin.getDatabaseManager().isCached(block.getLocation())) {
            plugin.getDatabaseManager().removeBlockEntry(block.getLocation());
            return;
        }
        LootContainer lootContainer = itemsAdderHook.hasLootContainer(blockName)
                ? itemsAdderHook.getLootContainer(blockName) : itemsAdderHook.getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(player, block, lootContainer, blockName);
    }

    @EventHandler
    public void itemsAdderBlockPlace(CustomBlockPlaceEvent event) {
        Block block = event.getBlock();
        String blockName = StringUtils.getItemsAdderName(event.getNamespacedID());

        if(plugin.getConfig().getStringList("world-blacklist").contains(block.getWorld().getName())) return;
        ItemsAdderBlockHook itemsAdderHook = (ItemsAdderBlockHook) plugin.getHookManager().getBlockProvider(BlockType.ITEMS_ADDER);
        if(!itemsAdderHook.hasLootContainer(blockName)) {
            if(itemsAdderHook.getDefaultLootContainer().getLootTables().isEmpty()) return;
            if(itemsAdderHook.getExcludedMaterials().contains(blockName)) return;
        }
        plugin.getDatabaseManager().addBlockEntry(block.getLocation());
    }

}
