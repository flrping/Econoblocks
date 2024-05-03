package dev.flrp.econoblocks.hook.item;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Builder;
import dev.flrp.econoblocks.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.MMOItemsItemProvider;

public class MMOItemsItemHook extends MMOItemsItemProvider implements Builder {

    private final Econoblocks plugin;

    public MMOItemsItemHook(Econoblocks plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        Configuration mmoItemsConfig = new Configuration(plugin, "hooks/MMOItems");
        mmoItemsConfig.load();

        Methods.buildHookMultipliersItems(mmoItemsConfig);
        Methods.buildHookMultiplierGroupsItems(mmoItemsConfig);

        mmoItemsConfig.save();
    }

    @Override
    public void reload() {
        build();
    }
}
