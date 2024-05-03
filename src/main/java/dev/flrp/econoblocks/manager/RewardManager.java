package dev.flrp.econoblocks.manager;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.api.event.BlockRewardEvent;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.util.multiplier.MultiplierGroup;
import dev.flrp.econoblocks.util.multiplier.MultiplierProfile;
import dev.flrp.espresso.condition.*;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.hook.item.ItemType;
import dev.flrp.espresso.table.*;
import dev.flrp.espresso.util.LootUtils;
import dev.flrp.espresso.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class RewardManager {

    private final Econoblocks plugin;

    private final HashMap<String, LootTable> availableTables = new HashMap<>();
    private final HashMap<Material, LootContainer> lootContainers = new HashMap<>();

    private LootContainer defaultLootContainer = new LootContainer();
    private final List<Material> excludedMaterials = new ArrayList<>();

    public RewardManager(Econoblocks plugin) {
        this.plugin = plugin;
        buildLootTables();
        if(plugin.getBlocks().getConfiguration().getConfigurationSection("blocks") != null) buildLootContainers();
        if(plugin.getBlocks().getConfiguration().contains("default")) buildDefaultLootContainer();
    }

    public LootTable getLootTable(String name) {
        return availableTables.get(name);
    }

    public HashMap<String, LootTable> getLootTables() {
        return availableTables;
    }

    public LootContainer getLootContainer(Material material) {
        return lootContainers.getOrDefault(material, defaultLootContainer);
    }

    public LootContainer getLootContainer(String material) {
        return lootContainers.getOrDefault(Material.matchMaterial(material), defaultLootContainer);
    }

    public boolean hasLootContainer(Material material) {
        return lootContainers.containsKey(material);
    }

    public boolean hasLootContainer(String material) {
        return lootContainers.containsKey(Material.matchMaterial(material));
    }

    public HashMap<Material, LootContainer> getLootContainers() {
        return lootContainers;
    }

    public LootContainer getDefaultLootContainer() {
        return defaultLootContainer;
    }

    public List<Material> getExcludedMaterials() {
        return excludedMaterials;
    }

    // Builders
    private void buildLootTables() {
        FileConfiguration config = plugin.getLootTables().getConfiguration();
        Set<String> tableSet = config.getConfigurationSection("tables").getKeys(false);

        // Loop through all the tables
        for(String table : tableSet) {
            // Make a new Table for current table
            LootTable lootTable = new LootTable(table, config.getDouble("tables." + table + ".weight"));
            // Loop through all the lootables in the table.
            for(String lootable : config.getConfigurationSection("tables." + table + ".drops").getKeys(false)) {

                Lootable loot = null;
                // Some helpful variables
                String section = "tables." + table + ".drops." + lootable;
                // Finding loot type.
                LootType lootType = LootType.getByName(config.getString(section + ".type"));
                if(lootType == LootType.NONE) continue;

                // Make loot based on type found.
                switch (lootType) {
                    case ECONOMY:
                        loot = LootUtils.createEconomyLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.ECONOMY_GIVEN);
                        break;
                    case ITEM:
                        loot = LootUtils.createItemLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.ITEM_GIVEN);
                        break;
                    case CUSTOM_ITEM:
                        loot = LootUtils.createCustomItemLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.ITEM_GIVEN);
                        break;
                    case POTION:
                        loot = LootUtils.createPotionEffectLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.POTION_GIVEN);
                        break;
                    case COMMAND:
                        loot = LootUtils.createCommandLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.COMMAND_GIVEN);
                        break;
                }
                if (loot == null || loot.getType() == null) continue;
                lootTable.addLoot(loot);
            }
            availableTables.put(table, lootTable);
        }
        Locale.log("Loaded &e" + availableTables.size() + " &rloot tables.");
    }

    private void buildLootContainers() {
        int modifiedTables = 0;
        Set<String> blockSet = plugin.getBlocks().getConfiguration().getConfigurationSection("blocks").getKeys(false);

        // Loop through all the mobs in file
        for(String block : blockSet) {

            Material material;
            try {
                material = Material.matchMaterial(block);
            } catch (Exception e) {
                Locale.log("&cInvalid material found (" + block +"), skipping.");
                continue;
            }
            LootContainer lootContainer = new LootContainer();

            // Get the tables for the mob
            Set<String> tableSet = plugin.getBlocks().getConfiguration().getConfigurationSection("blocks." + block + ".tables").getKeys(false);
            for(String tableNumber : tableSet) {

                // Boolean checks
                boolean hasTable = plugin.getBlocks().getConfiguration().contains("blocks." + block + ".tables." + tableNumber + ".table")
                        && availableTables.containsKey(plugin.getBlocks().getConfiguration().getString("blocks." + block + ".tables." + tableNumber + ".table"));
                boolean hasConditions = plugin.getBlocks().getConfiguration().contains("blocks." + block + ".tables." + tableNumber + ".conditions");
                boolean hasWeightOverride = plugin.getBlocks().getConfiguration().contains("blocks." + block + ".tables." + tableNumber + ".weight");

                if(!hasTable) continue;
                LootTable lootTable = availableTables.get(plugin.getBlocks().getConfiguration().getString("blocks." + block + ".tables." + tableNumber + ".table"));

                if(!hasConditions && !hasWeightOverride) {
                    lootContainer.addLootTable(lootTable);
                } else {
                    LootTable modifiedLootTable = lootTable.clone();
                    if(hasConditions) parseConditions(modifiedLootTable, plugin.getBlocks().getConfiguration().getConfigurationSection("blocks." + block + ".tables." + tableNumber));
                    if(hasWeightOverride) modifiedLootTable.setWeight(plugin.getBlocks().getConfiguration().getDouble("blocks." + block + ".tables." + tableNumber + ".weight"));
                    lootContainer.addLootTable(modifiedLootTable);
                    modifiedTables++;
                }

            }

            lootContainers.put(material, lootContainer);
        }

        Locale.log("Loaded &e" + lootContainers.size() + " &rloot containers for blocks.");
        Locale.log("Loaded &e" + modifiedTables + " &rmodified loot tables.");
    }

    private void buildDefaultLootContainer() {
        LootContainer lootContainer = new LootContainer();
        Set<String> tableSet = plugin.getBlocks().getConfiguration().getConfigurationSection("default.tables").getKeys(false);
        for(String tableNumber : tableSet) {
            LootTable lootTable = availableTables.get(plugin.getBlocks().getConfiguration().getString("default.tables." + tableNumber + ".table"));
            if(lootTable == null) continue;

            boolean hasConditions = plugin.getBlocks().getConfiguration().contains("default.tables." + tableNumber + ".conditions");
            boolean hasWeightOverride = plugin.getBlocks().getConfiguration().contains("default.tables." + tableNumber + ".weight");

            if(!hasConditions && !hasWeightOverride) {
                lootContainer.addLootTable(lootTable);
            } else {
                LootTable modifiedLootTable = lootTable.clone();
                if(hasConditions) parseConditions(modifiedLootTable, plugin.getBlocks().getConfiguration().getConfigurationSection("default.tables." + tableNumber));
                if(hasWeightOverride) modifiedLootTable.setWeight(plugin.getBlocks().getConfiguration().getDouble("default.tables." + tableNumber + ".weight"));
                lootContainer.addLootTable(modifiedLootTable);
            }
        }

        if(plugin.getBlocks().getConfiguration().contains("default.excludes")) {
            for(String block : plugin.getBlocks().getConfiguration().getStringList("default.excludes")) {
                try {
                    excludedMaterials.add(Material.valueOf(block.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    Locale.log("&cInvalid material type (" + block + ") for exclusion in default loot table. Skipping.");

                }
            }
        }

        defaultLootContainer = lootContainer;
        Locale.log("Default loot created with &e" + lootContainer.getLootTables().size() + " &rtables.");
    }

    // Conditionals
    public void parseConditions(LootTable lootTable, ConfigurationSection tableSection) {
        List<String> conditionSet = tableSection.getStringList("conditions");
        List<Condition> conditions = new ArrayList<>();
        for(String entry : conditionSet) {
            if (entry.startsWith("[")) {
                ConditionType conditionType = ConditionType.getByName(entry.substring(1, entry.indexOf("]")));
                String value = entry.substring(entry.indexOf("]") + 2);
                switch (conditionType) {
                    case WITH:
                        parseWithCondition(lootTable, tableSection, value, conditions);
                        break;
                    case BIOME:
                        parseBiomeCondition(lootTable, tableSection, value, conditions);
                        break;
                    case PERMISSION:
                        parsePermissionCondition(lootTable, value, conditions);
                        break;
                    case WORLD:
                        parseWorldCondition(lootTable, tableSection, value, conditions);
                        break;
                }
                lootTable.setConditions(conditions);
            }
        }
    }

    private void parseBiomeCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        Biome biome;
        try {
            biome = Biome.valueOf(value);
        } catch (IllegalArgumentException e) {
            Locale.log("&cInvalid biome (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
            return;
        }
        BiomeCondition biomeCondition;
        if(lootTable.getConditions().stream().anyMatch(condition -> condition instanceof BiomeCondition)) {
            biomeCondition = (BiomeCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof BiomeCondition).findFirst().get();
            biomeCondition.addBiome(biome);
        } else {
            biomeCondition = new BiomeCondition();
            biomeCondition.addBiome(biome);
            conditions.add(biomeCondition);
        }
    }

    private void parseWithCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        ItemType itemType;
        if(value.contains(":")) {
            String hookString = value.split(":")[0];
            itemType = ItemType.getByName(hookString) == ItemType.NONE ? null : ItemType.getByName(hookString);
            if(itemType == null) {
                Locale.log("&cInvalid Hook (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
                return;
            }
            value = value.split(":")[1];
        } else {
            itemType = ItemType.NONE;
            try {
                Material.valueOf(value.toUpperCase());
                value = value.toUpperCase();
            } catch (IllegalArgumentException e) {
                Locale.log("&cInvalid material (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
                return;
            }
        }
        WithConditionExtended withCondition;
        if(lootTable.getConditions().stream().anyMatch(condition -> condition instanceof WithConditionExtended)) {
            withCondition = (WithConditionExtended) lootTable.getConditions().stream().filter(condition -> condition instanceof WithConditionExtended).findFirst().get();
            withCondition.addMaterial(itemType, value);
        } else {
            withCondition = new WithConditionExtended();
            withCondition.addMaterial(itemType, value);
            conditions.add(withCondition);
        }
    }

    private void parsePermissionCondition(LootTable lootTable, String value, List<Condition> conditions) {
        PermissionCondition permissionCondition;
        if(lootTable.getConditions().stream().anyMatch(condition -> condition instanceof PermissionCondition)) {
            permissionCondition = (PermissionCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof PermissionCondition).findFirst().get();
            permissionCondition.setPermission(value);
        } else {
            permissionCondition = new PermissionCondition();
            permissionCondition.setPermission(value);
            conditions.add(permissionCondition);
        }
    }

    private void parseWorldCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        WorldCondition worldCondition;
        if(Bukkit.getWorld(value) == null) {
            Locale.log("&cInvalid world (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
            return;
        }
        if(lootTable.getConditions().stream().anyMatch(condition -> condition instanceof WorldCondition)) {
            worldCondition = (WorldCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof WorldCondition).findFirst().get();
            worldCondition.addWorld(value);
        } else {
            worldCondition = new WorldCondition();
            worldCondition.addWorld(value);
            conditions.add(worldCondition);
        }
    }

    private boolean meetsConditions(Player player, Block block, LootTable lootTable) {
        if(lootTable.getConditions().isEmpty()) return true;
        for(Condition condition : lootTable.getConditions()) {
            if(condition instanceof WithConditionExtended) {
                ItemType type = ItemType.NONE;
                ItemStack item = player.getInventory().getItemInMainHand();
                String itemName = item.getType().toString();
                if(!plugin.getHookManager().getItemProviders().isEmpty()) {
                    for (ItemProvider provider : plugin.getHookManager().getItemProviders()) {
                        if (provider.isCustomItem(item)) {
                            itemName = provider.getCustomItemName(item);
                            type = provider.getType();
                        }
                    }
                }
                if(!((WithConditionExtended) condition).check(type, itemName)) return false;
            }
            if(condition instanceof BiomeCondition) {
                if(!((BiomeCondition) condition).check(block.getBiome())) return false;
            }
            if(condition instanceof PermissionCondition) {
                if(!((PermissionCondition) condition).check(player)) return false;
            }
            if(condition instanceof WorldCondition) {
                if(!((WorldCondition) condition).check(block.getWorld().getName())) return false;
            }
        }
        return true;
    }

    // Multipliers
    private double calculateMultiplier(Player player, Block block, MultiplierProfile profile, String blockName) {
        double amount = 1;
        amount *= getWorldMultiplier(profile, player.getWorld().getUID());
        if(!plugin.getHookManager().getBlockProviders().isEmpty() && Material.matchMaterial(blockName) == null) {
            amount *= getCustomBlockMultiplier(profile, blockName);
        } else {
            amount *= getBlockMultiplier(profile, block);
        }
        boolean isCustomTool = false;
        if(!plugin.getHookManager().getItemProviders().isEmpty()) {
            for(ItemProvider provider : plugin.getHookManager().getItemProviders()) {
                if(provider.isCustomItem(player.getInventory().getItemInMainHand())) {
                    amount *= getCustomToolMultiplier(profile, provider.getCustomItemName(player.getInventory().getItemInMainHand()));
                    isCustomTool = true;
                    break;
                }
            }
        }
        if(!isCustomTool) {
            amount *= getToolMultiplier(profile, player.getInventory().getItemInMainHand().getType());
        }
        return amount;
    }

    public double getBlockMultiplier(MultiplierProfile profile, Block block) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        Material material = block.getType();
        if(profile.getMaterials().containsKey(material)) {
            return profile.getMaterials().get(material);
        } else
        if(group != null && group.getMaterials().containsKey(material)) {
            return group.getMaterials().get(material);
        }
        return 1;
    }

    public double getCustomBlockMultiplier(MultiplierProfile profile, String customBlock) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if(profile.getCustomMaterials().containsKey(customBlock)) {
            return profile.getCustomMaterials().get(customBlock);
        } else
        if(group != null && group.getCustomMaterials().containsKey(customBlock)) {
            return group.getCustomMaterials().get(customBlock);
        }
        return 1;
    }

    public double getToolMultiplier(MultiplierProfile profile, Material material) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if(profile.getTools().containsKey(material)) {
            return profile.getTools().get(material);
        } else
        if(group != null && group.getTools().containsKey(material)) {
            return group.getTools().get(material);
        }
        return 1;
    }

    public double getCustomToolMultiplier(MultiplierProfile profile, String customTool) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if(profile.getCustomTools().containsKey(customTool)) {
            return profile.getCustomTools().get(customTool);
        } else
        if(group != null && group.getCustomTools().containsKey(customTool)) {
            return group.getCustomTools().get(customTool);
        }
        return 1;
    }

    public double getWorldMultiplier(MultiplierProfile profile, UUID uuid) {
        if(profile.getWorlds().containsKey(uuid)) {
            return profile.getWorlds().get(uuid);
        }
        return 1;
    }

    // Handlers
    public void handleLootReward(Player player, Block block, LootContainer lootContainer, String blockName) {
        LootTable lootTable = lootContainer.rollLootTable();
        if (lootTable == null || !meetsConditions(player, block, lootTable)) {
            return;
        }

        Lootable loot = lootTable.roll();
        if (loot == null) {
            return;
        }

        LootResult result = loot.generateResult();
        result.setLootTable(lootTable);

        switch (loot.getType()) {
            case ECONOMY:
                handleEconomyReward(player, block, (LootableEconomy) loot, result, blockName);
                break;
            case COMMAND:
                handleCommandReward(player, block, (LootableCommand) loot, result, blockName);
                break;
            case POTION:
                handlePotionReward(player, block, (LootablePotionEffect) loot, result, blockName);
                break;
            case CUSTOM_ITEM:
                handleCustomItemReward(player, block, (LootableCustomItem) loot, result, blockName);
                break;
            case ITEM:
                handleItemReward(player, block, (LootableItem) loot, result, blockName);
                break;
        }
    }

    private void handleEconomyReward(Player player, Block block, LootableEconomy loot, LootResult result, String blockName) {
        MultiplierProfile profile = plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId());
        double multiplier = calculateMultiplier(player, block, profile, blockName);
        double base = result.getAmount();

        result.setAmount(base);

        double amount = base * multiplier;

        BlockRewardEvent event = new BlockRewardEvent(player, block, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        plugin.getHookManager().getEconomyProvider(loot.getEconomyType()).deposit(player, amount);
        if(!plugin.getToggleList().contains(player.getUniqueId()))
            plugin.getMessageManager().sendMessage(player, block, result, multiplier, amount, blockName);
    }

    private void handleCommandReward(Player player, Block block, LootableCommand loot, LootResult result, String blockName) {
        BlockRewardEvent event = new BlockRewardEvent(player, block, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        for (int j = 0; j < result.getAmount(); j++) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loot.getCommand().replace("{player}", player.getName()));
        }
        if(!plugin.getToggleList().contains(player.getUniqueId()))
            plugin.getMessageManager().sendMessage(player, block, result, blockName);
    }

    private void handlePotionReward(Player player, Block block, LootablePotionEffect loot, LootResult result, String blockName) {
        BlockRewardEvent event = new BlockRewardEvent(player, block, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        player.addPotionEffect(new PotionEffect(loot.getEffectType(), (int) (result.getAmount() * 20), loot.getAmplifier()));
        if(!plugin.getToggleList().contains(player.getUniqueId()))
            plugin.getMessageManager().sendMessage(player, block, result, blockName);
    }

    private void handleCustomItemReward(Player player, Block block, LootableCustomItem loot, LootResult result, String blockName) {
        BlockRewardEvent event = new BlockRewardEvent(player, block, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        plugin.getHookManager().getItemProvider(loot.getItemType()).giveItem(player, loot.getCustomItemName(), (int) result.getAmount());
        if(!plugin.getToggleList().contains(player.getUniqueId()))
            plugin.getMessageManager().sendMessage(player, block, result, blockName);
    }

    private void handleItemReward(Player player, Block block, LootableItem loot, LootResult result, String blockName) {
        BlockRewardEvent event = new BlockRewardEvent(player, block, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        ItemStack item = loot.getItemStack();
        item.setAmount((int) result.getAmount());
        player.getInventory().addItem(item);
        if(!plugin.getToggleList().contains(player.getUniqueId()))
            plugin.getMessageManager().sendMessage(player, block, result, blockName);
    }

}
