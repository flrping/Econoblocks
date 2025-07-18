package dev.flrp.econoblocks.manager;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.multiplier.MultiplierProfile;
import dev.flrp.econoblocks.util.chunk.ChunkLocation;
import dev.flrp.espresso.storage.behavior.SQLStorageBehavior;
import dev.flrp.espresso.storage.exception.ProviderException;
import dev.flrp.espresso.storage.provider.SQLStorageProvider;
import dev.flrp.espresso.storage.provider.SQLiteStorageProvider;
import dev.flrp.espresso.storage.provider.StorageType;
import dev.flrp.espresso.storage.query.*;
import org.bukkit.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {

    private final Econoblocks plugin;
    private final Set<Location> blockCache = new HashSet<>(), rawCache = new HashSet<>();
    private final Map<ChunkLocation, Set<Location>> chunkCache = new ConcurrentHashMap<>();
    private final Map<UUID, MultiplierProfile> playerCache = new ConcurrentHashMap<>();
    private StorageType storageType;
    private SQLStorageProvider provider;
    private SQLStorageBehavior behavior;

    public DatabaseManager(Econoblocks plugin) {
        this.plugin = plugin;
        connect();
        init();
    }

    private void resolveStorageType() {
        storageType = StorageType.getByName(plugin.getConfig().getString("database.provider"));

        if (storageType == null || storageType == StorageType.NONE) {
            Locale.log("Invalid storage type in config. Using default SQLite.");
            storageType = StorageType.SQLITE;
        }
    }

    private void connect() {
        resolveStorageType();

        try {
            switch (storageType) {
                case YAML:
                    throw new UnsupportedOperationException("YAML storage is not supported for this plugin.");
                case JSON:
                    throw new UnsupportedOperationException("JSON storage is not supported for this plugin.");
                case SQLITE:
                    File sqlite = new File(plugin.getDataFolder(), "database.db");
                    if (!sqlite.exists()) {
                        try {
                            sqlite.createNewFile();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    provider = new SQLiteStorageProvider(plugin.getLogger(), sqlite);
                    break;
                default:
                    provider = new SQLStorageProvider(
                            plugin.getLogger(),
                            plugin.getConfig().getString("database.host"),
                            plugin.getConfig().getInt("database.port"),
                            plugin.getConfig().getString("database.database"),
                            plugin.getConfig().getString("database.username"),
                            plugin.getConfig().getString("database.password"),
                            storageType
                    );
            }

            behavior = (SQLStorageBehavior) provider.getBehavior();
            behavior.open();
        } catch (Exception e) {
            Locale.log("Unable to connect to the database: " + e.getMessage());
            Locale.log("Storage-related features are disabled until fixed. When storage isn't connected, you can use /reload to try again.");
        }
    }

    private void multiplierInit() {
        List<SQLColumn> multiplierColumns = Arrays.asList(
                new SQLColumn("user", SQLType.STRING).notNull(),
                new SQLColumn("context", SQLType.STRING).notNull(),
                new SQLColumn("multiplier", SQLType.DOUBLE).notNull(),
                new SQLColumn("type", SQLType.STRING).notNull()
                        .check("type IN ('MATERIAL', 'TOOL', 'WORLD')")
        );
        String createMultiplierTable = provider.getDialect().createTable("multipliers", multiplierColumns);
        try {
            behavior.query(createMultiplierTable);

            SelectQueryBuilder selectMultipliers = new SelectQueryBuilder("multipliers", provider).columns("*");
            behavior.queryMap(selectMultipliers.build(), rs -> {
                UUID uuid = UUID.fromString(rs.getString("user"));
                MultiplierProfile mp = playerCache.computeIfAbsent(uuid, MultiplierProfile::new);

                String type = rs.getString("type");
                switch (type) {
                    case "MATERIAL":
                        mp.getMaterials().put(Material.matchMaterial(rs.getString("context")), rs.getDouble("multiplier"));
                        break;
                    case "TOOL":
                        mp.getTools().put(Material.matchMaterial(rs.getString("context")), rs.getDouble("multiplier"));
                        break;
                    case "WORLD":
                        mp.getWorlds().put(UUID.fromString(rs.getString("context")), rs.getDouble("multiplier"));
                        break;
                }
                return null;
            });
        } catch (ProviderException e) {
            Locale.log("Unable to create/manage the multiplier table in the database: " + e.getMessage());
        }
    }

    private void customMultiplierInit() {
        List<SQLColumn> customMultiplierColumns = Arrays.asList(
                new SQLColumn("user", SQLType.STRING).notNull(),
                new SQLColumn("context", SQLType.STRING).notNull(),
                new SQLColumn("multiplier", SQLType.DOUBLE).notNull(),
                new SQLColumn("type", SQLType.STRING).notNull()
                        .check("type IN ('MATERIAL', 'TOOL')")
        );
        String createCustomMultiplierTable = provider.getDialect().createTable("custom_multipliers", customMultiplierColumns);
        try {
            behavior.query(createCustomMultiplierTable);

            SelectQueryBuilder selectCustomMultipliers = new SelectQueryBuilder("custom_multipliers", provider).columns("*");
            behavior.queryMap(selectCustomMultipliers.build(), rs -> {
                UUID uuid = UUID.fromString(rs.getString("user"));
                MultiplierProfile mp = playerCache.computeIfAbsent(uuid, MultiplierProfile::new);

                String customType = rs.getString("type");
                switch (customType) {
                    case "MATERIAL":
                        mp.getCustomMaterials().put(rs.getString("context"), rs.getDouble("multiplier"));
                        break;
                    case "TOOL":
                        mp.getCustomTools().put(rs.getString("context"), rs.getDouble("multiplier"));
                        break;
                }
                return null;
            });
        } catch (ProviderException e) {
            Locale.log("Unable to create/manage the custom multiplier table in the database: " + e.getMessage());
        }
    }

    private void blockInit() {
        List<SQLColumn> blockColumns = Arrays.asList(
                new SQLColumn("w", SQLType.STRING).notNull(),
                new SQLColumn("x", SQLType.INT).notNull(),
                new SQLColumn("y", SQLType.INT).notNull(),
                new SQLColumn("z", SQLType.INT).notNull(),
                new SQLColumn("d", SQLType.DATE).notNull().defaultValue("current_date")
        );
        String createBlockTable = provider.getDialect().createTable("blocks", blockColumns);
        SelectQueryBuilder selectBlocks = new SelectQueryBuilder("blocks", provider).columns("*");
        AtomicInteger skipped = new AtomicInteger();

        try {
            behavior.query(createBlockTable);

            if (plugin.getConfig().getInt("checks.storage.expiry") > 0) {
                DeleteQueryBuilder removeStatement = new DeleteQueryBuilder("blocks", provider);
                if (provider.getType() == StorageType.SQLITE) {
                    removeStatement.where("d <= date('now', '-" + plugin.getConfig().getInt("checks.storage.expiry") + " day')");
                } else {
                    removeStatement.where("d <= now() - interval " + plugin.getConfig().getInt("checks.storage.expiry") + " day");
                }
                removeStatement.execute();
            }

            behavior.queryMap(selectBlocks.build(), rs -> {
                UUID worldId = UUID.fromString(rs.getString("w"));
                World world = Bukkit.getWorld(worldId);
                if (world == null) {
                    skipped.getAndIncrement();
                }

                Location location = new Location(
                        world,
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z")
                );

                ChunkLocation chunkLocation = new ChunkLocation(location);
                chunkCache.computeIfAbsent(chunkLocation, __ -> new HashSet<>()).add(location);
                rawCache.add(location);
                return null;
            });
        } catch (ProviderException e) {
            Locale.log("Unable to create/manage the block table in the database: " + e.getMessage());
        }

        Locale.log("Loaded &e" + rawCache.size() + " &rblocks from the database.");
        if (skipped.get() > 0) {
            Locale.log("Skipped &c" + skipped + " &rblocks due to invalid worlds.");
        }
    }

    private void init() {
        if (provider == null || !provider.isConnected()) return;

        multiplierInit();
        customMultiplierInit();
        Locale.log("Loaded &e" + playerCache.size() + " &rmultiplier profiles from the database.");

        // Stops early if storage persistence is disabled
        if (!plugin.getConfig().getBoolean("checks.storage.enabled")) return;
        blockInit();
    }

    public void refresh() {
        if(provider.isConnected()) return;
        connect();
        init();
    }

    public SQLStorageProvider getStorageProvider() {
        return provider;
    }

    public Set<Location> getBlockCache() {
        return blockCache;
    }

    public Map<ChunkLocation, Set<Location>> getChunkCache() {
        return chunkCache;
    }

    public boolean isCached(Location location) {
        return blockCache.contains(location);
    }

    public void addChunkEntries(Chunk chunk) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(chunk));
        if (locations != null) {
            blockCache.addAll(locations);
        }
    }

    public void removeChunkEntries(Chunk chunk) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(chunk));
        if (locations != null) {
            blockCache.removeAll(locations);
        }
    }

    public void addBlockEntry(Location location) {
        ChunkLocation chunkLocation = new ChunkLocation(location);
        Set<Location> locations = chunkCache.get(chunkLocation);
        if (locations != null) {
            locations.add(location);
        } else {
            Set<Location> blocks = new HashSet<>();
            blocks.add(location);
            chunkCache.put(chunkLocation, blocks);
        }
        blockCache.add(location);

        if (provider == null || !provider.isConnected()) return;
        if (plugin.getConfig().getBoolean("checks.storage.enabled")) {
            runAsync(() -> {
                        try {
                            new InsertQueryBuilder("blocks", provider)
                                    .column("w", location.getWorld().getUID().toString())
                                    .column("x", location.getX())
                                    .column("y", location.getY())
                                    .column("z", location.getZ())
                                    .execute();
                        } catch (ProviderException e) {
                            Locale.log("Unable to add a block to the database. This is a bug. Report this to the developer. Error: " + e.getMessage());
                        }
                    }
            );
        }
    }

    public void removeBlockEntry(Location location) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(location));
        if (locations != null) {
            blockCache.remove(location);
            locations.remove(location);

            if (provider == null || !provider.isConnected()) return;
            if (plugin.getConfig().getBoolean("checks.storage.enabled")) {
                runAsync(() -> {
                            try {
                                new DeleteQueryBuilder("blocks", provider)
                                        .where("w=? AND x=? AND y=? AND z=?", location.getWorld().getUID().toString(), location.getX(), location.getY(), location.getZ())
                                        .execute();
                            } catch (ProviderException e) {
                                Locale.log("Unable to remove a block from the database. This is a bug. Report this to the developer. Error: " + e.getMessage());
                            }
                        }
                );
            }
        }
    }

    public Map<UUID, MultiplierProfile> getPlayerCache() {
        return playerCache;
    }

    public boolean isCached(UUID uuid) {
        return playerCache.containsKey(uuid);
    }

    public MultiplierProfile createMultiplierProfile(UUID uuid) {
        MultiplierProfile multiplierProfile = new MultiplierProfile(uuid);
        playerCache.put(uuid, multiplierProfile);
        return multiplierProfile;
    }

    public MultiplierProfile getMultiplierProfile(UUID uuid) {
        if (playerCache.containsKey(uuid)) {
            return playerCache.get(uuid);
        }
        MultiplierProfile multiplierProfile = new MultiplierProfile(uuid);
        playerCache.put(uuid, multiplierProfile);
        return multiplierProfile;
    }

    public void addBlockMultiplier(UUID uuid, Material material, double multiplier) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "add", uuid, material.name(), "MATERIAL", multiplier);
    }

    public void addToolMultiplier(UUID uuid, Material material, double multiplier) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "add", uuid, material.name(), "TOOL", multiplier);
    }

    public void addWorldMultiplier(UUID uuid, UUID world, double multiplier) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "add", uuid, world.toString(), "WORLD", multiplier);
    }

    public void updateBlockMultiplier(UUID uuid, Material material, double multiplier) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "update", uuid, material.name(), "MATERIAL", multiplier);
    }

    public void updateToolMultiplier(UUID uuid, Material material, double multiplier) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "update", uuid, material.name(), "TOOL", multiplier);
    }

    public void updateWorldMultiplier(UUID uuid, UUID world, double multiplier) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "update", uuid, world.toString(), "WORLD", multiplier);
    }

    public void removeBlockMultiplier(UUID uuid, Material material) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "remove", uuid, material.name(), "MATERIAL", null);
    }

    public void removeToolMultiplier(UUID uuid, Material material) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "remove", uuid, material.name(), "TOOL", null);
    }

    public void removeWorldMultiplier(UUID uuid, UUID world) {
        applyMultiplierOperation(MultiplierTable.STANDARD, "remove", uuid, world.toString(), "WORLD", null);
    }

    public void addCustomBlockMultiplier(UUID uuid, String block, double multiplier) {
        applyMultiplierOperation(MultiplierTable.CUSTOM, "add", uuid, block, "MATERIAL", multiplier);
    }

    public void addCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        applyMultiplierOperation(MultiplierTable.CUSTOM, "add", uuid, tool, "TOOL", multiplier);
    }

    public void updateCustomBlockMultiplier(UUID uuid, String block, double multiplier) {
        applyMultiplierOperation(MultiplierTable.CUSTOM, "update", uuid, block, "MATERIAL", multiplier);
    }

    public void updateCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        applyMultiplierOperation(MultiplierTable.CUSTOM, "update", uuid, tool, "TOOL", multiplier);
    }

    public void removeCustomBlockMultiplier(UUID uuid, String block) {
        applyMultiplierOperation(MultiplierTable.CUSTOM, "remove", uuid, block, "MATERIAL", null);
    }

    public void removeCustomToolMultiplier(UUID uuid, String tool) {
        applyMultiplierOperation(MultiplierTable.CUSTOM, "remove", uuid, tool, "TOOL", null);
    }

    private void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    private enum MultiplierTable {
        STANDARD("multipliers"),
        CUSTOM("custom_multipliers");

        public final String name;
        MultiplierTable(String name) {
            this.name = name;
        }
    }

    private void applyMultiplierOperation(
            MultiplierTable table,
            String operation,
            UUID uuid,
            String context,
            String type,
            Double multiplier
    ) {
        runAsync(() -> {
            try {
                switch (operation) {
                    case "add":
                        new InsertQueryBuilder(table.name, provider)
                                .column("user", uuid.toString())
                                .column("context", context)
                                .column("multiplier", multiplier)
                                .column("type", type)
                                .execute();
                        break;
                    case "update":
                        new UpdateQueryBuilder(table.name, provider)
                                .set("multiplier", multiplier)
                                .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                                .execute();
                        break;
                    case "remove":
                        new DeleteQueryBuilder(table.name, provider)
                                .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                                .execute();
                        break;
                }
            } catch (ProviderException e) {
                Locale.log("Unable to " + operation + " a " + (table == MultiplierTable.STANDARD ? "" : "custom ") +
                        "multiplier. This is a bug. Report this to the developer. Error: " + e.getMessage());
            }
        });
    }

}
