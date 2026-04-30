package operations;

import db.EncryptedFileDAO;
import model.EncryptedFileRecord;
import service.SecurityService;
import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class DecryptOperation implements FileOperation {
    private final Path file;
    private final EncryptedFileDAO dao;
    private final SecurityService security;
    private boolean executed = false;

    // FIX #4: Store the encrypted bytes + metadata so undo can restore the
    // encrypted state directly, instead of calling new EncryptOperation()
    // which would create mutual recursion and generate a NEW key (wrong behaviour).
    private byte[] encryptedSnapshot;
    private byte[] wrappedKeySnapshot;
    private byte[] ivSnapshot;

    public DecryptOperation(Path file) {
        this.file = file;
        this.dao = new EncryptedFileDAO();
        this.security = SecurityService.getInstance();
    }

    @Override
    public void execute() throws Exception {
        Optional<EncryptedFileRecord> recordOpt = dao.findByPath(file.toString());
        if (recordOpt.isEmpty()) {
            throw new Exception("No encryption metadata found for: " + file);
        }

        EncryptedFileRecord record = recordOpt.get();

        // Save encrypted snapshot before we overwrite the file
        encryptedSnapshot  = Files.readAllBytes(file);
        wrappedKeySnapshot = record.getWrappedAesKey();
        ivSnapshot         = record.getIv();

        SecretKey aesKey = security.decryptAESKey(record.getWrappedAesKey());
        byte[] decryptedData = security.decryptFile(encryptedSnapshot, aesKey, record.getIv());

        Path tempFile = file.resolveSibling(file.getFileName().toString() + ".tmp");
        Files.write(tempFile, decryptedData);
        Files.delete(file);
        Files.move(tempFile, file);

        dao.delete(file.toString());
        executed = true;
    }

    @Override
    public void undo() throws Exception {
        // FIX: Restore saved ciphertext and re-insert the original DB record —
        // no recursive EncryptOperation call, no new key generation.
        if (executed && encryptedSnapshot != null) {
            Path tempFile = file.resolveSibling(file.getFileName().toString() + ".tmp");
            Files.write(tempFile, encryptedSnapshot);
            Files.delete(file);
            Files.move(tempFile, file);
            dao.insert(file.toString(), wrappedKeySnapshot, ivSnapshot);
            executed = false;
            encryptedSnapshot  = null;
            wrappedKeySnapshot = null;
            ivSnapshot         = null;
        }
    }

    @Override
    public double getProgress() {
        return executed ? 100.0 : 0.0;
    }
}
