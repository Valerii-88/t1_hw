package ru.t1.feature3.threadpool;

public final class Feature3Demo {
    private Feature3Demo() {
    }

    public static void main(String[] args) throws InterruptedException {
        CustomThreadPool threadPool = new CustomThreadPool(2);

        for (int taskNumber = 1; taskNumber <= 5; taskNumber++) {
            int currentTaskNumber = taskNumber;
            threadPool.execute(() -> {
                String workerName = Thread.currentThread().getName();
                System.out.println("Задача " + currentTaskNumber + " запущена в потоке " + workerName);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Задача " + currentTaskNumber + " завершена в потоке " + workerName);
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination();
        System.out.println("Пул потоков завершил работу");
    }
}
