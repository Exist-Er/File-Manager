package service;

import operations.FileOperation;
import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {

    private static final int MAX_HISTORY = 20;
    private final Deque<FileOperation> history = new ArrayDeque<>();

    // FIX #1: Atomic check-and-pop inside a single synchronized block
    // Previously, isEmpty() and pop() were separate synchronized calls,
    // creating a window where another thread could drain the deque between them.
    public synchronized void push(FileOperation op) {
        if (history.size() >= MAX_HISTORY) {
            history.removeLast(); // removes oldest entry (back of deque)
        }
        history.push(op); // pushes to front
    }

    public synchronized void undo() {
        // FIX: Single synchronized block - check and pop atomically
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
