package dev.flrp.econoblocks.hook.block;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Builder;
import dev.flrp.econoblocks.listener.OraxenListener;
import dev.flrp.econoblocks.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.block.OraxenBlockProvider;
import dev.flrp.espresso.table.LootContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OraxenBlockHook extends OraxenBlockProvider implements Builder {

    private final Econoblocks plugin;
    private final HashMap<String, LootContainer> oraxenRewards = new HashMap<>();
    private LootContainer defaultLootContainer = new LootContainer();
    private final List<String> excludedMaterials = new ArrayList<>();

    public OraxenBlockHook(Econoblocks plugin) {
        super();
        this.plugin = plugin;
        build();
        plugin.getServer().getPluginManager().registerEvents(new OraxenListener(plugin), plugin);
    }

    public LootContainer getLootContainer(String name) {
        return oraxenRewards.get(name);
    }

    public boolean hasLootContainer(String name) {
        return oraxenRewards.containsKey(name);
    }

    public HashMap<String, LootContainer> getLootContainers() {
        return oraxenRewards;
    }

    public LootContainer getDefaultLootContainer() {
        return defaultLootContainer;
    }

    public List<String> getExcludedMaterials() {
        return excludedMaterials;
    }

    @Override
    public void build() {
        Configuration oraxenFile = new Configuration(plugin, "hooks/Oraxen");
        oraxenFile.load();

        List<String> blockNames = new ArrayList<>(getCustomBlockNames());

        Methods.buildHookMultipliersBlocks(oraxenFile);
        Methods.buildHookBlocks(oraxenFile, blockNames);
        Methods.buildHookMultiplierGroupsBlocks(oraxenFile);
        Methods.buildRewardList(oraxenFile, oraxenRewards, "Oraxen");
        Methods.buildDefaultLootContainer(oraxenFile, defaultLootContainer, excludedMaterials);

        oraxenFile.save();
    }

    @Override
    public void reload() {
        oraxenRewards.clear();
        excludedMaterials.clear();
        defaultLootContainer = new LootContainer();
        build();
    }

}
