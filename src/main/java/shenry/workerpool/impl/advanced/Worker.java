package shenry.workerpool.impl.advanced;

/**
 * Used by {@link AdvancedTaskExecutor} for executing client's tasks.
 * Tasks are stored in {@link TaskQueue}. If there is no task in
 * worker's queue, worker executes tasks from other workers.
 * A new thread is created, all tasks are executed one by one in this thread.
 */
class Worker {
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

    private void threadRun() {

        while (!Thread.currentThread().isInterrupted()) {
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
        }
    }
}
