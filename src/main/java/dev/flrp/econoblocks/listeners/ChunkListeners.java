package dev.flrp.econoblocks.listeners;

import dev.flrp.econoblocks.Econoblocks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListeners implements Listener {

    private final Econoblocks plugin;

    public ChunkListeners(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.getDatabaseManager().addChunkEntries(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.getDatabaseManager().removeChunkEntries(event.getChunk());
    }

}