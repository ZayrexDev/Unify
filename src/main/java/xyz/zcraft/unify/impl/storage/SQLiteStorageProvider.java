package xyz.zcraft.unify.impl.storage;

import net.fabricmc.loader.api.FabricLoader;
import xyz.zcraft.unify.api.Lockable;
import xyz.zcraft.unify.api.StorageProvider;

import java.sql.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class SQLiteStorageProvider extends StorageProvider implements Lockable {
    private Connection conn;

    @Override
    public void init(HashMap<String, Object> args, Set<String> fields) {
        if (args.get("path") == null) throw new IllegalArgumentException("No database provided");
        final String s = "jdbc:sqlite:" + FabricLoader.getInstance().getConfigDir().resolve("unify").resolve(args.get("path").toString()).toAbsolutePath();
        try {
            conn = DriverManager.getConnection(s);
            PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS lock(uuid TEXT PRIMARY KEY, data TEXT);");
            stmt.executeUpdate();
            stmt.close();
            for (String field : fields) {
                stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + field + "(uuid TEXT PRIMARY KEY, data TEXT);");
                stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public void save(String uuid, String fieldName, String data) {
        try {
            var stmt = conn.prepareStatement("DELETE FROM " + fieldName + " WHERE uuid=?;");
            stmt.setString(1, uuid);

            stmt.executeUpdate();

            stmt.close();


            stmt = conn.prepareStatement("INSERT INTO " + fieldName + " VALUES(?,?)");
            stmt.setString(1, uuid);
            stmt.setString(2, data);

            stmt.executeUpdate();

            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error in saving binding data", e);
        }
    }

    @Override
    public String get(String uuid, String fieldName) {
        try {
            String result;
            final PreparedStatement stmt = conn.prepareStatement("SELECT data FROM " + fieldName + " WHERE uuid=?");
            stmt.setString(1, uuid);

            result = stmt.executeQuery().getString(1);

            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close connection", e);
        }
    }

    @Override
    public void lock(String serverUuid, String uuid) {
        if(isLocked(uuid)) throw new RuntimeException("Database already locked");
        try {
            var stmt = conn.prepareStatement("INSERT INTO lock VALUES(?,?)");
            stmt.setString(1, uuid);
            stmt.setString(2, serverUuid);

            stmt.executeUpdate();

            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLockedBy(String serverUuid, String uuid) {
        try {
            String result;
            final PreparedStatement stmt = conn.prepareStatement("SELECT data FROM lock WHERE uuid=?");
            stmt.setString(1, uuid);

            result = stmt.executeQuery().getString(1);

            stmt.close();
            return Objects.equals(serverUuid, result);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLocked(String uuid) {
        try {
            final PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM (SELECT data FROM lock WHERE uuid=?)");
            stmt.setString(1, uuid);

            int row = stmt.executeQuery().getInt(1);

            stmt.close();
            return row != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unlock(String serverUuid, String uuid) {
        if(!isLockedBy(serverUuid, uuid)) throw new RuntimeException("Database not locked by " + serverUuid);
        try {
            var stmt = conn.prepareStatement("DELETE FROM lock WHERE uuid=?;");
            stmt.setString(1, uuid);

            stmt.executeUpdate();

            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
