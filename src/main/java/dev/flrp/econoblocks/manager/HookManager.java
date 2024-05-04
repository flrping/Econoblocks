package dev.flrp.econoblocks.manager;

import com.google.inject.Inject;
import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.espresso.hook.block.BlockProvider;
import dev.flrp.espresso.hook.block.BlockType;
import dev.flrp.espresso.hook.economy.EconomyProvider;
import dev.flrp.espresso.hook.economy.EconomyType;
import dev.flrp.espresso.hook.hologram.HologramProvider;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.hook.item.ItemType;

import javax.annotation.Nullable;
import java.util.Set;

public class HookManager {

    private final Set<BlockProvider> blockProviders;
    private final Set<EconomyProvider> economyProviders;
    private final HologramProvider hologramProvider;
    private final Set<ItemProvider> itemProviders;

    @Inject
    public HookManager(Econoblocks plugin, Set<BlockProvider> blockProviders, Set<EconomyProvider> economyProviders, @Nullable HologramProvider hologramProvider, Set<ItemProvider> itemProviders) {
        this.blockProviders = blockProviders;
        this.economyProviders = economyProviders;
        this.hologramProvider = hologramProvider;
        this.itemProviders = itemProviders;
    }

    public Set<BlockProvider> getBlockProviders() {
        return blockProviders;
    }

    public Set<EconomyProvider> getEconomyProviders() {
        return economyProviders;
    }

    public HologramProvider getHologramProvider() {
        return hologramProvider;
    }

    public Set<ItemProvider> getItemProviders() {
        return itemProviders;
    }

    public BlockProvider getBlockProvider(String name) {
        for(BlockProvider blockProvider : blockProviders) {
            if(blockProvider.getName().equalsIgnoreCase(name)) {
                return blockProvider;
            }
        }
        return null;
    }

    public BlockProvider getBlockProvider(BlockType type) {
        for(BlockProvider blockProvider : blockProviders) {
            if(blockProvider.getType() == type) {
                return blockProvider;
            }
        }
        return null;
    }

    public EconomyProvider getEconomyProvider(String name) {
        for(EconomyProvider economyProvider : economyProviders) {
            if(economyProvider.getName().equalsIgnoreCase(name)) {
                return economyProvider;
            }
        }
        return null;
    }

    public EconomyProvider getEconomyProvider(EconomyType type) {
        for(EconomyProvider economyProvider : economyProviders) {
            if(economyProvider.getType() == type) {
                return economyProvider;
            }
        }
        return null;
    }

    public ItemProvider getItemProvider(String name) {
        for(ItemProvider itemProvider : itemProviders) {
            if(itemProvider.getName().equalsIgnoreCase(name)) {
                return itemProvider;
            }
        }
        return null;
    }

    public ItemProvider getItemProvider(ItemType type) {
        for(ItemProvider itemProvider : itemProviders) {
            if(itemProvider.getType() == type) {
                return itemProvider;
            }
        }
        return null;
    }

}
