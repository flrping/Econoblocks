package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.utils.ChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private Connection connection;
    private final Set<Location> cache = new HashSet<>();
    private final Set<Location> rawCache = new HashSet<>();
    private final Set<Location> removalCache = new HashSet<>();
    private final HashMap<ChunkLocation, Set<Location>> chunkCache = new HashMap<>();

    public DatabaseManager(Econoblocks plugin) {
        try {
            if(!plugin.getConfig().getBoolean("checks.storage.enabled")) return;

            // Finding sqlite
            Class.forName("org.sqlite.JDBC");
            System.out.println("[Econoblocks] Found SQLite. Unlocking database usage if applicable.");

            // Create connection
            File databaseFile = new File(plugin.getDataFolder(), "database.db");
            if(!databaseFile.exists()) {
                databaseFile.createNewFile();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);

            // Checking if table exists
            Statement tableStatement = connection.createStatement();
            String createTable = "CREATE TABLE IF NOT EXISTS blocks (" +
                    "w varchar(36) NOT NULL," +
                    "x int NOT NULL," +
                    "y int NOT NULL," +
                    "z int NOT NULL," +
                    "d date NOT NULL default(current_date))";
            tableStatement.executeUpdate(createTable);
            tableStatement.close();

            // Deleting entries
            if(plugin.getConfig().getInt("checks.storage.expiry") > 0) {
                Statement removeStatement = connection.createStatement();
                String removeEntries = "DELETE FROM blocks WHERE d <= date('now', '-" + plugin.getConfig().getInt("checks.storage.expiry") + " day')";
                removeStatement.executeUpdate(removeEntries);
                removeStatement.close();
            }

            // Loading entries
            Statement statement = connection.createStatement();
            String sql = "SELECT * FROM blocks;";
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                Location location = new Location(Bukkit.getWorld(UUID.fromString(rs.getString("w"))), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
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
            System.out.println("[Econoblocks] Loaded " + rawCache.size() + " stored blocks from the database.");

        } catch (ClassNotFoundException e) {
            System.out.println("[Econoblocks] Could not find SQLite, blocks will not be stored on restart.");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[Econoblocks] Could not create the database file.");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Set<Location> getCache() {
        return cache;
    }

    public Set<Location> getRemovalCache() {
        return removalCache;
    }

    public HashMap<ChunkLocation, Set<Location>> getChunkCache() {
        return chunkCache;
    }

    public boolean isCached(Location location) {
        return cache.contains(location);
    }

    public void addChunkEntries(Chunk chunk) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(chunk));
        if(locations != null) {
            cache.addAll(locations);
        }
    }

    public void removeChunkEntries(Chunk chunk) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(chunk));
        if(locations != null) {
            cache.removeAll(locations);
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
        cache.add(location);
    }

    public void removeBlockEntry(Location location) {
        Set<Location> locations = chunkCache.get(new ChunkLocation(location));
        if(locations != null) {
            cache.remove(location);
            locations.remove(location);
            removalCache.add(location);
        }
    }

    public void save() {
        try {
            Statement statement = connection.createStatement();
            for(Location location : removalCache) {
                String sql = "DELETE FROM blocks WHERE " + "w='" + location.getWorld().getUID() + "' AND x=" + location.getX() + " AND y=" + location.getY() + " AND z=" + location.getZ() + ";";
                statement.executeUpdate(sql);
            }
            for(Location location : cache) {
                if(rawCache.contains(location)) continue;
                String sql = "INSERT INTO blocks (w,x,y,z) VALUES " +
                        "('" + location.getWorld().getUID() + "', " + location.getX() + ", " + location.getY() + ", " + location.getZ() + ");";
                statement.executeUpdate(sql);
            }
            System.out.println("[Econoblocks] Database saved.");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

}
