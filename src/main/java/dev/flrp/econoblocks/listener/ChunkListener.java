package dev.flrp.econoblocks.listener;

import dev.flrp.econoblocks.Econoblocks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    private final Econoblocks plugin;

    public ChunkListener(Econoblocks plugin) {
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