package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import model.EncryptedFileRecord;

/**
 * EncryptedFileDAO
 * Handles all database operations for the encrypted_files table.
 * This table is the ONLY place the wrapped AES key and IV are stored.
 * Without a row here, the file on disk CANNOT be decrypted.
 * Called by:
 *   FileExplorerService.encryptFile() → insert()   (after HybridEncryptor runs)
 *   FileExplorerService.decryptFile() → findByPath() + delete()
 *   FileListPanel (icon overlay)      → isEncrypted()
 * Table columns:
 *   id              INTEGER   PK AUTOINCREMENT
 *   file_path       TEXT      UNIQUE — absolute path of the .enc file on disk
 *   wrapped_aes_key BLOB      RSA-OAEP encrypted AES-256 key bytes
 *   iv              BLOB      12-byte AES-GCM nonce bytes
 *   encrypted_at    DATETIME  auto-set by SQLite
 */
public class EncryptedFileDAO {

    private final Connection conn;

    public EncryptedFileDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void insert(String filePath, byte[] wrappedAesKey, byte[] iv) {
        String sql = """
                INSERT OR REPLACE INTO encrypted_files
                    (file_path, wrapped_aes_key, iv)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, filePath);
            ps.setBytes (2, wrappedAesKey);
            ps.setBytes (3, iv);
            ps.executeUpdate();

            System.out.println("[EncryptedFileDAO] Saved key for: " + filePath);
        } catch (SQLException e) {
            System.err.println("[EncryptedFileDAO] insert error: " + e.getMessage());
        }
    }

    public Optional<EncryptedFileRecord> findByPath(String filePath) {
        String sql = """
                SELECT file_path, wrapped_aes_key, iv
                FROM encrypted_files
                WHERE file_path = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, filePath);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new EncryptedFileRecord(
                            rs.getString("file_path"),
                            rs.getBytes ("wrapped_aes_key"),
                            rs.getBytes ("iv")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EncryptedFileDAO] findByPath error: " + e.getMessage());
        }

        return Optional.empty();
    }

    public void delete(String filePath) {
        String sql = "DELETE FROM encrypted_files WHERE file_path = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, filePath);
            int rows = ps.executeUpdate();
            System.out.println("[EncryptedFileDAO] Deleted " + rows + " record(s) for: " + filePath);
        } catch (SQLException e) {
            System.err.println("[EncryptedFileDAO] delete error: " + e.getMessage());
        }
    }

    public void updatePath(String oldPath, String newPath) {
        String sql = "UPDATE encrypted_files SET file_path = ? WHERE file_path = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPath);
            ps.setString(2, oldPath);
            ps.executeUpdate();
            System.out.println("[EncryptedFileDAO] Path updated: " + oldPath + " -> " + newPath);
        } catch (SQLException e) {
            System.err.println("[EncryptedFileDAO] updatePath error: " + e.getMessage());
        }
    }

    public boolean isEncrypted(String filePath) {
        String sql = "SELECT COUNT(*) FROM encrypted_files WHERE file_path = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, filePath);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[EncryptedFileDAO] isEncrypted error: " + e.getMessage());
            return false;
        }
    }
}
