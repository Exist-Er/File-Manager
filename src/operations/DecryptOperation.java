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
        byte[] encryptedData = Files.readAllBytes(file);

        SecretKey aesKey = security.decryptAESKey(record.getWrappedAesKey());
        byte[] decryptedData = security.decryptFile(encryptedData, aesKey, record.getIv());

        Path tempFile = file.resolveSibling(file.getFileName().toString() + ".tmp");
        Files.write(tempFile, decryptedData);
        Files.delete(file);
        Files.move(tempFile, file);

        dao.delete(file.toString());
        executed = true;
    }

    @Override
    public void undo() throws Exception {
        if (executed) {
            new EncryptOperation(file).execute();
            executed = false;
        }
    }

    @Override
    public double getProgress() {
        return executed ? 100.0 : 0.0;
    }
}
