package shenry.workerpool.impl.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used by {@link AdvancedTaskExecutor} for executing client's tasks.
 * Tasks are stored in {@link TaskQueue}. If there is no task in
 * worker's queue, worker executes tasks from other workers.
 * A new thread is created, all tasks are executed one by one in this thread.
 */
class Worker {
    private final static Logger logger = LoggerFactory.getLogger(Worker.class);

    private ExternalTaskGetter externalTaskGetter;

    private final TaskQueue tasks = new TaskQueue();

    private final Thread thread;

    public Worker() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadRun();
            }
        });
    }

    public ExternalTaskGetter getExternalTaskGetter() {
        return externalTaskGetter;
    }

    public void setExternalTaskGetter(ExternalTaskGetter externalTaskGetter) {
        this.externalTaskGetter = externalTaskGetter;
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void addTask(int clientId, Runnable task) {
        tasks.add(clientId, task);
    }

    public Runnable getNextTask() {
        return tasks.nextTask();
    }

    /**
     * TODO Remember clientIds that have tasks that were executed with errors.
     * TODO Do not execute task of a client if previous task of this client was executed with errors.
     */
    private void threadRun() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Runnable task = getNextTask();

                if (task == null) {
                    if (externalTaskGetter != null) {
                        task = externalTaskGetter.getExternalTask(this);
                    }

                    if (task == null) {
                        // There can be Thread.sleep according to task adding speed.
                        Thread.yield();
/*
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
*/
                    }
                }

                if (task != null) {
                    task.run();
                }
            } catch (Exception ex) {
                logger.error("Error executing tasks", ex);
            }
        }
    }
}
