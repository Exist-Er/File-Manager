package operations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class CopyOperation implements FileOperation {
    private final Path source;
    private final Path destination;
    private long totalBytes;

    // FIX #3: volatile ensures ProgressPublisher (polling from another thread)
    // always reads the latest written value, not a CPU-cached stale value.
    private volatile long bytesCopied;

    public CopyOperation(Path source, Path destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void execute() throws Exception {
        totalBytes = Files.size(source);
        bytesCopied = 0;

        byte[] buffer = new byte[8192];
        try (InputStream in = new FileInputStream(source.toFile());
             OutputStream out = new FileOutputStream(destination.toFile())) {

            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                bytesCopied += bytesRead; // visible to polling thread due to volatile
            }
        }
    }

    @Override
    public void undo() throws Exception {
        Files.deleteIfExists(destination);
    }

    @Override
    public double getProgress() {
        if (totalBytes == 0) return 100.0;
        return (double) bytesCopied / totalBytes * 100.0;
    }
}
