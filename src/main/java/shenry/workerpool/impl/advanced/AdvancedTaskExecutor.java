package shenry.workerpool.impl.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shenry.workerpool.TaskExecutor;
import shenry.workerpool.impl.simple.SimpleTaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link TaskExecutor} realization. Each worker has its own tasks queue,
 * all tasks for one client in normal work are executed by one worker.
 * Worker for client tasks is determined by clientId hashcode. If worker
 * doesn't have tasks to execute, it executes tasks of other workers.
 *
 * <p>Pros:
 * <ul>
 *     <li>Each worker has its own queue, so in normal work there is no global
 *     (for all workers) lock while getting tasks from queue.</li>
 *     <li>It is not possible situation when tasks from several clients are executed
 *     by only one worker while other workers do nothing. If worker doesn't have tasks
 *     it executes tasks of other workers.</li>
 * </ul>
 *
 * <p>Cons:
 * <ul>
 *      <li>Realization is not so such simple as in SimpleTaskExecutor.</li>
 *      <li>In this realization, when we search tasks from other workers we
 *      always iterate workers from first to last, so tasks from first workers
 *      will have more priority then tasks from last workers. Possible solution:
 *      iterate workers from some random position (not from the beginning).</li>
 * </ul>
 */
public class AdvancedTaskExecutor implements TaskExecutor {

    private static Logger logger = LoggerFactory.getLogger(SimpleTaskExecutor.class);

    private final List<Worker> workers;

    private boolean started = false;

    /**
     * Creates {@link AdvancedTaskExecutor} with the give workers count.
     *
     * @param workersCount workers count.
     * @throws IllegalArgumentException if workers count is incorrect.
     */
    public AdvancedTaskExecutor(int workersCount) throws IllegalArgumentException {
        if (workersCount <= 0) {
            throw new IllegalArgumentException("Incorrect workers count");
        }

        List<Worker> workersTmp = new ArrayList<Worker>(workersCount);
        for (int i = 0; i < workersCount; i++) {
            workersTmp.add(new Worker());
        }

        workers = Collections.unmodifiableList(workersTmp);
        OtherWorkersTaskGetter externalTaskGetter = new OtherWorkersTaskGetter(workers);

        for (Worker worker : workers) {
            worker.setExternalTaskGetter(externalTaskGetter);
        }
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
        workers.get(workerIndex).addTask(clientId, task);

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
