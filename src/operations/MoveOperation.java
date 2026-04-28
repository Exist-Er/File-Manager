package operations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MoveOperation implements FileOperation {
    private final Path source;
    private final Path destination;
    private final db.EncryptedFileDAO dao;
    private boolean moved = false;

    public MoveOperation(Path source, Path destination) {
        this.source = source;
        this.destination = destination;
        this.dao = new db.EncryptedFileDAO();
    }

    @Override
    public void execute() throws Exception {
        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        dao.updatePath(source.toString(), destination.toString());
        moved = true;
    }

    @Override
    public void undo() throws Exception {
        if (moved) {
            Files.move(destination, source, StandardCopyOption.REPLACE_EXISTING);
            dao.updatePath(destination.toString(), source.toString());
            moved = false;
        }
    }

    @Override
    public double getProgress() {
        return moved ? 100.0 : 0.0;
    }
}
