package operations;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipOperation implements FileOperation {
    private final Path source;
    private final Path zipTarget;
    private double progress = 0.0;

    public ZipOperation(Path source, Path zipTarget) {
        this.source = source;
        this.zipTarget = zipTarget;
    }

    @Override
    public void execute() throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipTarget.toFile()))) {
            Files.walk(source).filter(p -> !Files.isDirectory(p)).forEach(p -> {
                try {
                    zos.putNextEntry(new ZipEntry(source.relativize(p).toString()));
                    Files.copy(p, zos);
                    zos.closeEntry();
                    progress = 50.0;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        progress = 100.0;
    }

    @Override
    public void undo() throws Exception {
        Files.deleteIfExists(zipTarget);
    }

    @Override
    public double getProgress() {
        return progress;
    }
}
