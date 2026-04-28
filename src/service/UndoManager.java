package service;

import operations.FileOperation;
import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {

    private static final int MAX_HISTORY = 20;
    private final Deque<FileOperation> history = new ArrayDeque<>();

    public synchronized void push(FileOperation op) {
        if (history.size() >= MAX_HISTORY) {
            history.removeLast();
        }
        history.push(op);
    }

    public synchronized void undo() {
        if (!history.isEmpty()) {
            FileOperation op = history.pop();
            try {
                op.undo();
            } catch (Exception e) {
                System.err.println("[Undo] Failed: " + e.getMessage());
            }
        }
    }

    public synchronized boolean canUndo() {
        return !history.isEmpty();
    }

    public synchronized int size() {
        return history.size();
    }
}
