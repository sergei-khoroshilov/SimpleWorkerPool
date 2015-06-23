package shenry.workerpool.impl.advanced;

import java.util.List;

/**
 * {@link ExternalTaskGetter} implementation.
 */
class OtherWorkersTaskGetter implements ExternalTaskGetter {

    private final List<Worker> workers;

    public OtherWorkersTaskGetter(List<Worker> workers) {
        this.workers = workers;
    }

    @Override
    public Runnable getExternalTask(Worker currentWorker) {
        Runnable task = null;

        for (Worker worker : workers) {
            if (worker != currentWorker) {
                task = worker.getNextTask();
                if (task != null) {
                    break;
                }
            }
        }

        return task;
    }
}
