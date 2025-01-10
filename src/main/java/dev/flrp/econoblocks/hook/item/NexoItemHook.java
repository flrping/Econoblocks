package dev.flrp.econoblocks.hook.item;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Builder;
import dev.flrp.econoblocks.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.NexoItemProvider;

public class NexoItemHook extends NexoItemProvider implements Builder {

    private final Econoblocks plugin;

    public NexoItemHook(Econoblocks plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        Configuration nexoConfig = new Configuration(plugin, "hooks/Nexo");
        nexoConfig.load();

        Methods.buildHookMultipliersItems(nexoConfig);
        Methods.buildHookMultiplierGroupsItems(nexoConfig);

        nexoConfig.save();
    }

    @Override
    public void reload() {
        build();
    }
}
