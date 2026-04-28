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

    public EncryptOperation(Path file) {
        this.file = file;
        this.dao = new EncryptedFileDAO();
        this.security = SecurityService.getInstance();
    }

    @Override
    public void execute() throws Exception {
        byte[] data = Files.readAllBytes(file);
        SecretKey aesKey = security.generateAESKey();
        byte[] iv = security.generateIV();

        byte[] encryptedData = security.encryptFile(data, aesKey, iv);
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
        if (executed) {
            new DecryptOperation(file).execute();
            executed = false;
        }
    }

    @Override
    public double getProgress() {
        return executed ? 100.0 : 0.0;
    }
}
