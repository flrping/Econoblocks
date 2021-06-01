package dev.flrp.econoblocks.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

public final class ChunkLocation {

    private final UUID uuid;
    private final int x;
    private final int z;

    public ChunkLocation(UUID uuid, int x, int z) {
        this.uuid = uuid;
        this.x = x;
        this.z = z;
    }

    public ChunkLocation(Location location) {
        this(location.getWorld().getUID(), location.getBlockX() >> 4,location.getBlockZ() >> 4);
    }

    public ChunkLocation(Chunk chunk) {
        this(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkLocation that = (ChunkLocation) o;
        return x == that.x && z == that.z && uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, x, z);
    }

    @Override
    public String toString() {
        return "ChunkLocation{" +
                "uuid=" + uuid +
                ", x=" + x +
                ", z=" + z +
                '}';
    }

}