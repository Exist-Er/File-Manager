package operations;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Basic Test Script to verify operations.
 * Compile: javac -d bin src/operations/*.java
 * Run: java -cp bin operations.OperationTest
 */
public class OperationTest {
    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("file_explorer_test");
        Path source = tempDir.resolve("source.txt");
        Path dest = tempDir.resolve("dest.txt");
        Files.write(source, "Hello World".getBytes());

        System.out.println("Testing CopyOperation...");
        CopyOperation copy = new CopyOperation(source, dest);
        copy.execute();
        if (Files.exists(dest)) System.out.println("  [PASS] Copy created file");

        System.out.println("Testing MoveOperation...");
        Path moved = tempDir.resolve("moved.txt");
        MoveOperation move = new MoveOperation(dest, moved);
        move.execute();
        if (Files.exists(moved) && !Files.exists(dest)) System.out.println("  [PASS] Move moved file");

        System.out.println("Testing DeleteOperation...");
        DeleteOperation delete = new DeleteOperation(moved);
        delete.execute();
        if (!Files.exists(moved)) System.out.println("  [PASS] Delete removed file");

        delete.undo();
        if (Files.exists(moved)) System.out.println("  [PASS] Delete undo restored file");

        System.out.println("\nMVP Operations Test Passed!");
    }
}
