package dev.flrp.econoblocks.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.hook.block.ItemsAdderBlockHook;
import dev.flrp.econoblocks.hook.block.OraxenBlockHook;
import dev.flrp.espresso.hook.block.BlockProvider;
import dev.flrp.espresso.hook.block.ItemsAdderBlockProvider;
import dev.flrp.espresso.hook.block.OraxenBlockProvider;

public class BlockModule extends AbstractModule {

    private final Econoblocks plugin;

    public BlockModule(Econoblocks plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Econoblocks.class).toInstance(plugin);
        Multibinder<BlockProvider> blockProviderMultibinder = Multibinder.newSetBinder(binder(), BlockProvider.class);
        if(plugin.getConfig().getBoolean("hooks.block.ItemsAdder") && plugin.getServer().getPluginManager().isPluginEnabled("ItemsAdder")) {
            plugin.getLogger().info("Hooking into ItemsAdder Blocks.");
            ItemsAdderBlockHook itemsAdderBlockHook = new ItemsAdderBlockHook(plugin);
            blockProviderMultibinder.addBinding().toInstance(itemsAdderBlockHook);
            bind(ItemsAdderBlockProvider.class).toInstance(itemsAdderBlockHook);
        }
        if(plugin.getConfig().getBoolean("hooks.block.Oraxen") && plugin.getServer().getPluginManager().isPluginEnabled("Oraxen")) {
            plugin.getLogger().info("Hooking into Oraxen Blocks.");
            OraxenBlockHook oraxenBlockHook = new OraxenBlockHook(plugin);
            blockProviderMultibinder.addBinding().toInstance(oraxenBlockHook);
            bind(OraxenBlockProvider.class).toInstance(oraxenBlockHook);
        }
    }


}
