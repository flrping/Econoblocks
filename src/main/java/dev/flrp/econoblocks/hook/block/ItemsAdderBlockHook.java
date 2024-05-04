package dev.flrp.econoblocks.hook.block;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Builder;
import dev.flrp.econoblocks.listener.ItemsAdderListener;
import dev.flrp.econoblocks.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.block.ItemsAdderBlockProvider;
import dev.flrp.espresso.table.LootContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemsAdderBlockHook extends ItemsAdderBlockProvider implements Builder {

    private final Econoblocks plugin;
    private final HashMap<String, LootContainer> itemsAdderRewards = new HashMap<>();
    private LootContainer defaultLootContainer = new LootContainer();
    private final List<String> excludedMaterials = new ArrayList<>();

    public ItemsAdderBlockHook(Econoblocks plugin) {
        super();
        this.plugin = plugin;
        build();
        plugin.getServer().getPluginManager().registerEvents(new ItemsAdderListener(plugin), plugin);
    }

    public LootContainer getLootContainer(String name) {
        return itemsAdderRewards.get(name);
    }

    public boolean hasLootContainer(String name) {
        return itemsAdderRewards.containsKey(name);
    }

    public HashMap<String, LootContainer> getLootContainers() {
        return itemsAdderRewards;
    }

    public LootContainer getDefaultLootContainer() {
        return defaultLootContainer;
    }

    public List<String> getExcludedMaterials() {
        return excludedMaterials;
    }

    @Override
    public void build() {
        Configuration itemsAdderFile = new Configuration(plugin, "hooks/ItemsAdder");
        itemsAdderFile.load();

        Methods.buildHookMultipliersBlocks(itemsAdderFile);
        Methods.buildHookBlocks(itemsAdderFile);
        Methods.buildHookMultiplierGroupsBlocks(itemsAdderFile);
        Methods.buildRewardList(itemsAdderFile, itemsAdderRewards, "ItemsAdder");
        Methods.buildDefaultLootContainer(itemsAdderFile, defaultLootContainer, excludedMaterials);

        itemsAdderFile.save();
    }

    @Override
    public void reload() {
        itemsAdderRewards.clear();
        excludedMaterials.clear();
        defaultLootContainer = new LootContainer();
        build();
    }

}
