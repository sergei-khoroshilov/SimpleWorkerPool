package shenry.workerpool.impl.advanced;

/**
 * Interface for task 'stealing' from other workers.
 * The object of this interface is used in {@link Worker} to get task
 * from other workers when there is no task to execute for this worker.
 */
interface ExternalTaskGetter {
    Runnable getExternalTask(Worker currentWorker);
}
