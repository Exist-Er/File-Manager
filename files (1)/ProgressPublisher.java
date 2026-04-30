package threading;

import operations.FileOperation;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.Future;

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
        // FIX #2: Submit to the shared ThreadPoolManager instead of spawning a raw Thread.
        // Raw threads bypass the pool, causing unbounded thread creation under load.
        Future<?> future = ThreadPoolManager.getInstance().submit(() -> {
            try {
                operation.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });

        // Poll progress while the pooled task is still running
        while (!future.isDone()) {
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
