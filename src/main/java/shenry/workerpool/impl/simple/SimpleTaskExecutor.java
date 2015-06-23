package shenry.workerpool.impl.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shenry.workerpool.TaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * {@link TaskExecutor} realization. Each worker has its own tasks queue,
 * all tasks for one client are executed by one worker. Each worker executes
 * tasks of several clients. Worker for client tasks is checked by clientId
 * hashcode.
 *
 * <p>Pros:
 * <ul>
 *     <li>Each client has its own queue, so there is no global (for all workers)
 *     lock while getting tasks from queue.</li>
 *     <li>Simple realization.</li>
 * </ul>
 *
 * <p>Cons:
 * <ul>
 *     <li>It is possible situation when tasks from several clients are executed
 *     by only one worker while other workers do nothing.</li>
 * </ul>
 */
public class SimpleTaskExecutor implements TaskExecutor {

    private class Worker {

        private final Thread thread;

        private final BlockingQueue<Runnable> tasks;

        /**
         * Creates {@link Worker} with initial tasks queue size.
         */
        public Worker() {
            this(100);
        }

        /**
         * Creates {@link Worker} with the given tasks queue size.
         *
         * @param capacity tasks queue size.
         * @throws IllegalArgumentException if tasks queue size is incorrect.
         */
        public Worker(int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("Incorrect capacity value.");
            }

            tasks = new ArrayBlockingQueue<Runnable>(capacity, true);

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    threadRun();
                }
            });
        }

        public void addTask(Runnable task) {
            try {
                tasks.put(task);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        public void start() {
            thread.start();
        }

        private void stop() {
            thread.interrupt();
        }

        private void removeAllTasks() {
            tasks.clear();
        }

        /**
         * TODO Remember clientIds that have tasks that were executed with errors.
         * TODO Do not execute task of a client if previous task of this client was executed with errors.
         */
        private void threadRun() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    tasks.take().run();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    logger.error("Error executing tasks", ex);
                }
            }
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SimpleTaskExecutor.class);

    private final List<Worker> workers;

    private boolean started = false;

    /**
     * Creates {@link SimpleTaskExecutor} with the give workers count.
     *
     * @param workersCount workers count.
     * @throws IllegalArgumentException if workers count is incorrect.
     */
    public SimpleTaskExecutor(int workersCount) throws IllegalArgumentException {
        if (workersCount <= 0) {
            throw new IllegalArgumentException("Incorrect workers count");
        }

        List<Worker> workersTmp = new ArrayList<Worker>(workersCount);
        for (int i = 0; i < workersCount; i++) {
            workersTmp.add(new Worker());
        }

        workers = Collections.unmodifiableList(workersTmp);
    }

    @Override
    public void start() {
        for (Worker worker : workers) {
            worker.start();
        }

        started = true;
        logger.info("Task executor started with {} workers", workers.size());
    }

    @Override
    public void stop() {
        for (Worker worker : workers) {
            worker.stop();
        }

        started = false;
        logger.info("Task executor stopped");
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void addTask(int clientId, Runnable task) {
        int workerIndex = getWorkerIndex(clientId);
        workers.get(workerIndex).addTask(task);

        logger.info("Task added for client {}", clientId);
    }

    @Override
    public void removeAllTasks() {
        for (Worker worker : workers) {
            worker.removeAllTasks();
        }

        logger.info("All tasks removed");
    }

    private int getWorkerIndex(int clientId) {
        return Math.abs(clientId) % workers.size();
    }
}
