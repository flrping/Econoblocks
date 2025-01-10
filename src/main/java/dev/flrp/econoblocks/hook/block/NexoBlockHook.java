package dev.flrp.econoblocks.hook.block;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.listener.NexoListener;
import dev.flrp.econoblocks.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.block.NexoBlockProvider;

import dev.flrp.econoblocks.configuration.Builder;
import dev.flrp.espresso.table.LootContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NexoBlockHook extends NexoBlockProvider implements Builder {

    private final Econoblocks plugin;
    private final HashMap<String, LootContainer> nexoRewards = new HashMap<>();
    private LootContainer defaultLootContainer = new LootContainer();
    private final List<String> excludedMaterials = new ArrayList<>();

    public NexoBlockHook(Econoblocks plugin) {
        super();
        this.plugin = plugin;
        build();
        plugin.getServer().getPluginManager().registerEvents(new NexoListener(plugin), plugin);
    }

    public LootContainer getLootContainer(String name) {
        return nexoRewards.get(name);
    }

    public boolean hasLootContainer(String name) {
        return nexoRewards.containsKey(name);
    }

    public HashMap<String, LootContainer> getLootContainers() {
        return nexoRewards;
    }

    public LootContainer getDefaultLootContainer() {
        return defaultLootContainer;
    }

    public List<String> getExcludedMaterials() {
        return excludedMaterials;
    }

    public void build() {
        Configuration nexoFile = new Configuration(plugin, "hooks/Nexo");
        nexoFile.load();

        List<String> blockNames = new ArrayList<>(getCustomBlockNames());

        Methods.buildHookMultipliersBlocks(nexoFile);
        Methods.buildHookBlocks(nexoFile, blockNames);
        Methods.buildHookMultiplierGroupsBlocks(nexoFile);
        Methods.buildRewardList(nexoFile, nexoRewards, "Nexo");
        Methods.buildDefaultLootContainer(nexoFile, defaultLootContainer, excludedMaterials);

        nexoFile.save();
    }

    @Override
    public void reload() {
        nexoRewards.clear();
        excludedMaterials.clear();
        defaultLootContainer = new LootContainer();
        build();
    }

}
