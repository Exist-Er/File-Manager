package operations;

import db.EncryptedFileDAO;
import service.SecurityService;
import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;

public class EncryptOperation implements FileOperation {
    private final Path file;
    private final EncryptedFileDAO dao;
    private final SecurityService security;
    private boolean executed = false;

    // FIX #4: Store original plaintext bytes so undo can directly restore them,
    // instead of calling new DecryptOperation() which creates mutual recursion
    // (EncryptOperation.undo -> DecryptOperation.execute -> and its undo calls EncryptOperation again).
    private byte[] originalData;

    public EncryptOperation(Path file) {
        this.file = file;
        this.dao = new EncryptedFileDAO();
        this.security = SecurityService.getInstance();
    }

    @Override
    public void execute() throws Exception {
        originalData = Files.readAllBytes(file); // save for undo

        SecretKey aesKey = security.generateAESKey();
        byte[] iv = security.generateIV();

        byte[] encryptedData = security.encryptFile(originalData, aesKey, iv);
        byte[] wrappedKey = security.encryptAESKey(aesKey);

        Path tempFile = file.resolveSibling(file.getFileName().toString() + ".tmp");
        Files.write(tempFile, encryptedData);
        Files.delete(file);
        Files.move(tempFile, file);

        dao.insert(file.toString(), wrappedKey, iv);
        executed = true;
    }

    @Override
    public void undo() throws Exception {
        // FIX: Directly restore saved plaintext — no recursive operation call
        if (executed && originalData != null) {
            Path tempFile = file.resolveSibling(file.getFileName().toString() + ".tmp");
            Files.write(tempFile, originalData);
            Files.delete(file);
            Files.move(tempFile, file);
            dao.delete(file.toString());
            executed = false;
            originalData = null;
        }
    }

    @Override
    public double getProgress() {
        return executed ? 100.0 : 0.0;
    }
}
