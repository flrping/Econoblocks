package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import dev.flrp.econoblocks.utils.chunk.ChunkLocation;
import dev.flrp.econoblocks.utils.multiplier.MultiplierProfile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DatabaseManager {

    private final Econoblocks plugin;
    private Connection connection;

    // Chunks
    private final Set<Location> blockCache = new HashSet<>(), rawCache = new HashSet<>();
    private final HashMap<ChunkLocation, Set<Location>> chunkCache = new HashMap<>();

    // Multipliers
    private final HashMap<UUID, MultiplierProfile> playerCache = new HashMap<>();

    public DatabaseManager(Econoblocks plugin) {
        this.plugin = plugin;
        try {
            // Finding sqlite
            Class.forName("org.sqlite.JDBC");
            Locale.log("&eSQLite &rfound. Unlocking database usage.");

            // Create connection
            File databaseFile = new File(plugin.getDataFolder(), "database.db");
            if(!databaseFile.exists()) {
                databaseFile.createNewFile();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);

            // Specific multiplier table
            Statement multiplierTableStatement = connection.createStatement();
            String createMultiplierTable = "CREATE TABLE IF NOT EXISTS multipliers (" +
                    "user varchar(36) NOT NULL," +
                    "context varchar NOT NULL," +
                    "multiplier double NOT NULL," +
                    "type varchar CHECK( type IN ('MATERIAL', 'TOOL', 'WORLD')) NOT NULL)";
            multiplierTableStatement.executeUpdate(createMultiplierTable);
            multiplierTableStatement.close();

            // Handling specific multipliers
            String MultiplierSql = "SELECT * FROM multipliers";
            Statement multiplierStatement = connection.createStatement();
            ResultSet  multiplierResultSet = multiplierStatement.executeQuery(MultiplierSql);
            while (multiplierResultSet.next()) {
                UUID uuid = UUID.fromString(multiplierResultSet.getString("user"));
                MultiplierProfile mp = new MultiplierProfile(uuid);
                playerCache.put(uuid, mp);

                switch (multiplierResultSet.getString("type")) {
                    case "MATERIAL":
                        mp.getMaterials().put(Material.matchMaterial(multiplierResultSet.getString("context")),
                                multiplierResultSet.getDouble("multiplier"));
                        break;
                    case "TOOL":
                        mp.getTools().put(Material.matchMaterial(multiplierResultSet.getString("context")),
                                multiplierResultSet.getDouble("multiplier"));
                        break;
                    case "WORLD":
                        mp.getWorlds().put(UUID.fromString(multiplierResultSet.getString("context")),
                                multiplierResultSet.getDouble("multiplier"));
                        break;
                    default:
                }
            }
            multiplierStatement.close();
            multiplierResultSet.close();

            // Specific custom multiplier table
            Statement customMultiplierTableStatement = connection.createStatement();
            String createCustomMultiplierTable = "CREATE TABLE IF NOT EXISTS custom_multipliers (" +
                    "user varchar(36) NOT NULL," +
                    "context varchar NOT NULL," +
                    "multiplier double NOT NULL," +
                    "type varchar CHECK( type IN ('MATERIAL', 'TOOL')) NOT NULL)";
            customMultiplierTableStatement.executeUpdate(createCustomMultiplierTable);
            customMultiplierTableStatement.close();

            // Handling specific custom multipliers
            String customMultiplierSql = "SELECT * FROM custom_multipliers";
            Statement customMultiplierStatement = connection.createStatement();
            ResultSet  customMultiplierResultSet = customMultiplierStatement.executeQuery(customMultiplierSql);
            while (customMultiplierResultSet.next()) {
                UUID uuid = UUID.fromString(customMultiplierResultSet.getString("user"));
                MultiplierProfile mp = playerCache.containsKey(uuid) ? playerCache.get(uuid) : new MultiplierProfile(uuid);
                playerCache.put(uuid, mp);

                switch (customMultiplierResultSet.getString("type")) {
                    case "MATERIAL":
                        mp.getCustomMaterials().put(customMultiplierResultSet.getString("context"),
                                customMultiplierResultSet.getDouble("multiplier"));
                        break;
                    case "TOOL":
                        mp.getCustomTools().put(customMultiplierResultSet.getString("context"),
                                customMultiplierResultSet.getDouble("multiplier"));
                        break;
                    default:
                }
            }
            customMultiplierStatement.close();
            customMultiplierResultSet.close();

            Locale.log("Loaded &e" + playerCache.size() + " &rmultiplier profiles from the database.");

            // Stopping if owner wishes not to store block data.
            if(!plugin.getConfig().getBoolean("checks.storage.enabled")) return;

            // LOADING PLACED BLOCKS
            // Block Table
            Statement blockTableStatement = connection.createStatement();
            String createBlockTable = "CREATE TABLE IF NOT EXISTS blocks (" +
                    "w varchar(36) NOT NULL," +
                    "x int NOT NULL," +
                    "y int NOT NULL," +
                    "z int NOT NULL," +
                    "d date NOT NULL default(current_date))";
            blockTableStatement.executeUpdate(createBlockTable);
            blockTableStatement.close();

            // Deleting entries
            if(plugin.getConfig().getInt("checks.storage.expiry") > 0) {
                Statement removeStatement = connection.createStatement();
                String removeEntries = "DELETE FROM blocks WHERE d <= date('now', '-" + plugin.getConfig().getInt("checks.storage.expiry") + " day')";
                removeStatement.executeUpdate(removeEntries);
                removeStatement.close();
            }

            // Loading entries
            String sql = "SELECT * FROM blocks;";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                // Making the location.
                Location location = new Location(Bukkit.getWorld(UUID.fromString(rs.getString("w"))), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
                // Checking if valid, mainly for servers with resource worlds.
                if(location.getWorld() == null) continue;
                // Find the location of the chunk and add to caches.
                ChunkLocation chunkLocation = new ChunkLocation(location);
                if(chunkCache.containsKey(chunkLocation)) {
                    chunkCache.get(chunkLocation).add(location);
                } else {
                    Set<Location> locations = new HashSet<>();
                    locations.add(location);
                    chunkCache.put(chunkLocation, locations);
                }
                rawCache.add(location);
            }
            rs.close();
            statement.close();
            Locale.log("Loaded &e" + rawCache.size() + " &rstored blocks from the database.");

        } catch (ClassNotFoundException e) {
            Locale.log("&cCould not find SQLite, some features will not work.");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Locale.log("&cCould not create the database file.");
        }
    }

    // Block / Chunk
    public Connection getConnection() {
        return connection;
    }

    public Set<Location> getBlockCache() {
        return blockCache;
    }

    public HashMap<ChunkLocation, Set<Location>> getChunkCache() {
        return chunkCache;
    }

    public boolean isCached(Location location) {
        return blockCache.contains(location);
    }

    public void addChunkEntries(Chunk chunk) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(chunk));
        if(locations != null) {
            blockCache.addAll(locations);
        }
    }

    public void removeChunkEntries(Chunk chunk) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(chunk));
        if(locations != null) {
            blockCache.removeAll(locations);
        }
    }

    public void addBlockEntry(Location location) {
        ChunkLocation chunkLocation = new ChunkLocation(location);
        Set<Location> locations = chunkCache.get(chunkLocation);
        if(locations != null) {
            locations.add(location);
        } else {
            Set<Location> blocks = new HashSet<>();
            blocks.add(location);
            chunkCache.put(chunkLocation, blocks);
        }
        blockCache.add(location);
        if(plugin.getConfig().getBoolean("checks.storage.enabled"))
            query("INSERT INTO blocks (w,x,y,z) VALUES " + "('" + location.getWorld().getUID() + "', " + location.getX() + ", " + location.getY() + ", " + location.getZ() + ");");
    }

    public void removeBlockEntry(Location location) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(location));
        if(locations != null) {
            blockCache.remove(location);
            locations.remove(location);
            if(plugin.getConfig().getBoolean("checks.storage.enabled"))
                query("DELETE FROM blocks WHERE " + "w='" + location.getWorld().getUID() + "' AND x=" + location.getX() + " AND y=" + location.getY() + " AND z=" + location.getZ() + ";");
        }
    }

    // Player
    public HashMap<UUID, MultiplierProfile> getPlayerCache() {
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
        if(playerCache.containsKey(uuid)) {
            return playerCache.get(uuid);
        }
        MultiplierProfile multiplierProfile = new MultiplierProfile(uuid);
        playerCache.put(uuid, multiplierProfile);
        return multiplierProfile;
    }

    private void addMultiplier(UUID uuid, String context, String type, double multiplier) {
        query("INSERT INTO multipliers (user,context,multiplier,type) VALUES ('" + uuid + "', '" + context + "', " + multiplier + " ,'" + type + "');");
    }

    public void addBlockMultiplier(UUID uuid, Material material, double multiplier) {
        addMultiplier(uuid, material.name(), "MATERIAL", multiplier);
    }

    public void addToolMultiplier(UUID uuid, Material material, double multiplier) {
        addMultiplier(uuid, material.name(), "TOOL", multiplier);
    }

    public void addWorldMultiplier(UUID uuid, UUID world, double multiplier) {
        addMultiplier(uuid, world.toString(), "WORLD", multiplier);
    }

    //

    private void updateMultiplier(UUID uuid, String context, String type, double multiplier) {
        query("UPDATE multipliers SET multiplier=" + multiplier + " WHERE user='" + uuid + "' AND context='" + context + "' AND type='" + type + "';");
    }

    public void updateBlockMultiplier(UUID uuid, Material material, double multiplier) {
        updateMultiplier(uuid, material.name(), "MATERIAL", multiplier);
    }

    public void updateToolMultiplier(UUID uuid, Material material, double multiplier) {
        updateMultiplier(uuid, material.name(), "TOOL", multiplier);
    }

    public void updateWorldMultiplier(UUID uuid, UUID world, double multiplier) {
        updateMultiplier(uuid, world.toString(), "WORLD", multiplier);
    }

    //

    private void removeMultiplier(UUID uuid, String context, String type) {
        query("DELETE FROM multipliers WHERE user='" + uuid + "' AND context='" + context + "' AND type='" + type + "';");
    }

    public void removeBlockMultiplier(UUID uuid, Material material) {
        removeMultiplier(uuid, material.name(), "MATERIAL");
    }

    public void removeToolMultiplier(UUID uuid, Material material) {
        removeMultiplier(uuid, material.name(), "TOOL");
    }

    public void removeWorldMultiplier(UUID uuid, UUID world) {
        removeMultiplier(uuid, world.toString(), "WORLD");
    }

    //

    public void addCustomMultiplier(UUID uuid, String context, String type, double multiplier) {
        query("INSERT INTO custom_multipliers (user,context,multiplier,type) VALUES ('" + uuid + "', '" + context + "', " + multiplier + " ,'" + type + "');");
    }

    public void addCustomBlockMultiplier(UUID uuid, String block, double multiplier) {
        addCustomMultiplier(uuid, block, "MATERIAL", multiplier);
    }

    public void addCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        addCustomMultiplier(uuid, tool, "TOOL", multiplier);
    }

    public void updateCustomMultiplier(UUID uuid, String context, String type, double multiplier) {
        query("UPDATE custom_multipliers SET multiplier=" + multiplier + " WHERE user='" + uuid + "' AND context='" + context + "' AND type='" + type + "';");
    }

    public void updateCustomBlockMultiplier(UUID uuid, String block, double multiplier) {
        updateCustomMultiplier(uuid, block, "MATERIAL", multiplier);
    }

    public void updateCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        updateCustomMultiplier(uuid, tool, "TOOL", multiplier);
    }

    public void removeCustomMultiplier(UUID uuid, String context, String type) {
        query("DELETE FROM custom_multipliers WHERE user='" + uuid + "' AND context='" + context + "' AND type='" + type + "';");
    }

    public void removeCustomBlockMultiplier(UUID uuid, String block) {
        removeCustomMultiplier(uuid, block, "MATERIAL");
    }

    public void removeCustomToolMultiplier(UUID uuid, String tool) {
        removeCustomMultiplier(uuid, tool, "TOOL");
    }

    private void query(String sql) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

}
