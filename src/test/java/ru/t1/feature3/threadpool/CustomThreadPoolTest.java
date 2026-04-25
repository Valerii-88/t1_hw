package ru.t1.feature3.threadpool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class CustomThreadPoolTest {
    @Test
    void executesTasksInSubmissionOrderWithSingleWorker() throws InterruptedException {
        CustomThreadPool threadPool = new CustomThreadPool(1);
        List<Integer> executionOrder = new CopyOnWriteArrayList<>();
        CountDownLatch done = new CountDownLatch(3);

        threadPool.execute(() -> {
            executionOrder.add(1);
            done.countDown();
        });
        threadPool.execute(() -> {
            executionOrder.add(2);
            done.countDown();
        });
        threadPool.execute(() -> {
            executionOrder.add(3);
            done.countDown();
        });

        threadPool.shutdown();
        Assertions.assertTrue(done.await(2, TimeUnit.SECONDS));
        threadPool.awaitTermination();

        Assertions.assertEquals(List.of(1, 2, 3), executionOrder);
    }

    @Test
    void executesAllQueuedTasksBeforeTermination() throws InterruptedException {
        CustomThreadPool threadPool = new CustomThreadPool(2);
        AtomicInteger completedTasks = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(4);

        for (int index = 0; index < 4; index++) {
            threadPool.execute(() -> {
                completedTasks.incrementAndGet();
                done.countDown();
            });
        }

        threadPool.shutdown();
        Assertions.assertTrue(done.await(2, TimeUnit.SECONDS));
        threadPool.awaitTermination();

        Assertions.assertEquals(4, completedTasks.get());
        Assertions.assertTrue(threadPool.isShutdown());
    }

    @Test
    void rejectsNewTasksAfterShutdown() throws InterruptedException {
        CustomThreadPool threadPool = new CustomThreadPool(1);

        threadPool.shutdown();

        Assertions.assertThrows(IllegalStateException.class, () -> threadPool.execute(() -> {
        }));
        threadPool.awaitTermination();
    }

    @Test
    void awaitTerminationWaitsUntilRunningTaskFinishes() throws InterruptedException {
        CustomThreadPool threadPool = new CustomThreadPool(1);
        CountDownLatch taskStarted = new CountDownLatch(1);
        CountDownLatch releaseTask = new CountDownLatch(1);
        AtomicBoolean taskFinished = new AtomicBoolean(false);
        AtomicBoolean terminationFinished = new AtomicBoolean(false);

        threadPool.execute(() -> {
            taskStarted.countDown();
            try {
                releaseTask.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            taskFinished.set(true);
        });

        Assertions.assertTrue(taskStarted.await(2, TimeUnit.SECONDS));
        threadPool.shutdown();

        Thread waiter = new Thread(() -> {
            try {
                threadPool.awaitTermination();
                terminationFinished.set(true);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.start();

        Thread.sleep(150);
        Assertions.assertFalse(terminationFinished.get());

        releaseTask.countDown();
        waiter.join(2_000);

        Assertions.assertTrue(taskFinished.get());
        Assertions.assertTrue(terminationFinished.get());
    }

    @Test
    void continuesProcessingAfterTaskFailure() throws InterruptedException {
        CustomThreadPool threadPool = new CustomThreadPool(1);
        List<Integer> completedTasks = new ArrayList<>();
        CountDownLatch done = new CountDownLatch(2);

        threadPool.execute(() -> {
            done.countDown();
            throw new RuntimeException("boom");
        });
        threadPool.execute(() -> {
            completedTasks.add(2);
            done.countDown();
        });

        threadPool.shutdown();
        Assertions.assertTrue(done.await(2, TimeUnit.SECONDS));
        threadPool.awaitTermination();

        Assertions.assertEquals(List.of(2), completedTasks);
    }

    @Test
    void rejectsNonPositiveCapacity() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CustomThreadPool(0));
    }
}
