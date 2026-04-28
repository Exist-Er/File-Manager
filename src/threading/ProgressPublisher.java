package threading;

import operations.FileOperation;
import javax.swing.SwingWorker;
import java.util.List;

public class ProgressPublisher extends SwingWorker<Void, Integer> {

    private final FileOperation operation;
    private final ProgressListener listener;

    public interface ProgressListener {
        void onProgress(int percent);
        void onDone();
    }

    public ProgressPublisher(FileOperation operation, ProgressListener listener) {
        this.operation = operation;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground() throws Exception {

        Thread workerThread = new Thread(() -> {
            try {
                operation.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        workerThread.start();

        while (workerThread.isAlive()) {
            publish((int) operation.getProgress());
            Thread.sleep(100);
        }
        publish(100);
        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {

        int latest = chunks.get(chunks.size() - 1);
        if (listener != null) listener.onProgress(latest);
    }

    @Override
    protected void done() {
        if (listener != null) listener.onDone();
    }
}
