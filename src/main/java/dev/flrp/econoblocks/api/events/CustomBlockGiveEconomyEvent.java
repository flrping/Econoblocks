package dev.flrp.econoblocks.api.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomBlockGiveEconomyEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;

    private final double amount;
    private final String identifier;

    public CustomBlockGiveEconomyEvent(double amount, String identifier) {
        this.amount = amount;
        this.identifier = identifier;
    }

    public double getAmount() {
        return amount;
    }

    public String getBlockIdentifier() {
        return identifier;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

}
