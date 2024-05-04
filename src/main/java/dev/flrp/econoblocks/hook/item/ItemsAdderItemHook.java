package dev.flrp.econoblocks.hook.item;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Builder;
import dev.flrp.econoblocks.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.ItemsAdderItemProvider;

public class ItemsAdderItemHook extends ItemsAdderItemProvider implements Builder {

    private final Econoblocks plugin;

    public ItemsAdderItemHook(Econoblocks plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        Configuration itemsAdderConfig = new Configuration(plugin, "hooks/ItemsAdder");
        itemsAdderConfig.load();

        Methods.buildHookMultipliersItems(itemsAdderConfig);
        Methods.buildHookMultiplierGroupsItems(itemsAdderConfig);

        itemsAdderConfig.save();
    }

    @Override
    public void reload() {
        build();
    }

}
