package ru.t1.feature3.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class CustomThreadPool {
    private final LinkedList<Runnable> taskQueue = new LinkedList<>();
    private final List<Thread> workers = new ArrayList<>();
    private volatile boolean shutdown;

    public CustomThreadPool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Thread pool capacity must be greater than 0");
        }

        for (int index = 0; index < capacity; index++) {
            Thread worker = new Thread(this::runWorker, "custom-thread-pool-worker-" + (index + 1));
            worker.start();
            workers.add(worker);
        }
    }

    public void execute(Runnable task) {
        Objects.requireNonNull(task, "task must not be null");

        synchronized (taskQueue) {
            if (shutdown) {
                throw new IllegalStateException("Thread pool is already shut down");
            }

            taskQueue.addLast(task);
            taskQueue.notify();
        }
    }

    public void shutdown() {
        synchronized (taskQueue) {
            shutdown = true;
            taskQueue.notifyAll();
        }
    }

    public void awaitTermination() throws InterruptedException {
        for (Thread worker : workers) {
            worker.join();
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    private void runWorker() {
        while (true) {
            Runnable task = takeNextTaskOrExit();
            if (task == null) {
                return;
            }

            try {
                task.run();
            } catch (Throwable ignored) {
                // Worker must stay alive even if a task fails.
            }
        }
    }

    private Runnable takeNextTaskOrExit() {
        synchronized (taskQueue) {
            while (taskQueue.isEmpty()) {
                if (shutdown) {
                    return null;
                }

                try {
                    taskQueue.wait();
                } catch (InterruptedException exception) {
                    if (shutdown) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }

            return taskQueue.removeFirst();
        }
    }
}
