package dev.flrp.econoblocks.hook.item;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Builder;
import dev.flrp.econoblocks.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.OraxenItemProvider;

public class OraxenItemHook extends OraxenItemProvider implements Builder {

    private final Econoblocks plugin;

    public OraxenItemHook(Econoblocks plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        Configuration oraxenConfig = new Configuration(plugin, "hooks/Oraxen");
        oraxenConfig.load();

        Methods.buildHookMultipliersItems(oraxenConfig);
        Methods.buildHookMultiplierGroupsItems(oraxenConfig);

        oraxenConfig.save();
    }

    @Override
    public void reload() {
        build();
    }

}
