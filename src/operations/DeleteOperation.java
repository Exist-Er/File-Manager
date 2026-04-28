package operations;

import java.nio.file.Files;
import java.nio.file.Path;

public class DeleteOperation implements FileOperation {
    private final Path target;
    private byte[] originalContent;
    private boolean deleted = false;

    public DeleteOperation(Path target) {
        this.target = target;
    }

    @Override
    public void execute() throws Exception {
        if (Files.exists(target)) {
            originalContent = Files.readAllBytes(target);
            Files.delete(target);
            deleted = true;
        }
    }

    @Override
    public void undo() throws Exception {
        if (deleted && originalContent != null) {
            Files.write(target, originalContent);
            deleted = false;
        }
    }

    @Override
    public double getProgress() {
        return deleted ? 100.0 : 0.0;
    }
}
