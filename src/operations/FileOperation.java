package operations;

public interface FileOperation {
    void execute() throws Exception;
    void undo() throws Exception;
    double getProgress();
}
