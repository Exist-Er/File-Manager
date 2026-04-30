package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * FavoritesDAO
 * Handles all database operations for the favorites table.
 * Called by:
 *   SidebarPanel  → getAll()      (load bookmarks on startup)
 *   FileListPanel → add()         (right-click → "Add to Favorites")
 *   SidebarPanel  → remove()      (remove button next to bookmark)
 *   FileListPanel → isFavorite()  (show filled/empty star icon)
 * Table columns:
 *   id   INTEGER  PK AUTOINCREMENT
 *   path TEXT     UNIQUE — full folder/file path
 *   name TEXT     display label shown in sidebar
 */
public class FavoritesDAO {

    private final Connection conn;

    public FavoritesDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void add(String path, String name) {
        String sql = "INSERT OR IGNORE INTO favorites (path, name) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.setString(2, name);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("[FavoritesDAO] Added: " + name + " → " + path);
            } else {
                System.out.println("[FavoritesDAO] Already exists (ignored): " + path);
            }
        } catch (SQLException e) {
            System.err.println("[FavoritesDAO] add error: " + e.getMessage());
        }
    }

    public void remove(String path) {
        String sql = "DELETE FROM favorites WHERE path = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            int rows = ps.executeUpdate();
            System.out.println("[FavoritesDAO] Removed " + rows + " favorite(s) for: " + path);
        } catch (SQLException e) {
            System.err.println("[FavoritesDAO] remove error: " + e.getMessage());
        }
    }

    public List<String[]> getAll() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT path, name FROM favorites ORDER BY name ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("path"),
                        rs.getString("name")
                });
            }
        } catch (SQLException e) {
            System.err.println("[FavoritesDAO] getAll error: " + e.getMessage());
        }

        return list;
    }

    public boolean isFavorite(String path) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE path = ?";

        // FIX #5: Wrap ResultSet in try-with-resources to prevent connection leak.
        // The original code called rs.getInt(1) and returned without ever closing rs.
        // Under repeated calls (e.g. rendering each file tile) this exhausts
        // SQLite's cursor limit and causes "too many open statements" errors.
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[FavoritesDAO] isFavorite error: " + e.getMessage());
            return false;
        }
    }
}
