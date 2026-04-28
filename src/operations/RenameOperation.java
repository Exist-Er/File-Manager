package operations;

import java.nio.file.Files;
import java.nio.file.Path;

public class RenameOperation implements FileOperation {
    private final Path source;
    private final Path target;
    private final db.EncryptedFileDAO dao;
    private boolean executed = false;

    public RenameOperation(Path source, Path target) {
        this.source = source;
        this.target = target;
        this.dao = new db.EncryptedFileDAO();
    }

    @Override
    public void execute() throws Exception {
        Files.move(source, target);
        dao.updatePath(source.toString(), target.toString());
        executed = true;
    }

    @Override
    public void undo() throws Exception {
        if (executed) {
            Files.move(target, source);
            dao.updatePath(target.toString(), source.toString());
            executed = false;
        }
    }

    @Override
    public double getProgress() {
        return executed ? 100.0 : 0.0;
    }
}
