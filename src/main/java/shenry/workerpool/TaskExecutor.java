package shenry.workerpool;

/**
 * TaskExecutor executes tasks for clients in several threads.
 * Tasks are executed for a client in the order in which they were added.
 * Only one task is executed for a client in one moment of time.
 */
public interface TaskExecutor {

    /**
     * Causes TaskExecutor to begin execute tasks.
     * Nothing happens if TaskExecutor is already started.
     */
    void start();

    /**
     * Causes TaskExecutor to stop execute tasks.
     * All tasks that were not executed will be executed after starting.
     * Nothing happens if TaskExecutor is already stopped.
     */
    void stop();

    /**
     * Check if TaskExecutor is started.
     */
    boolean isStarted();

    /**
     * Add client task for execution.
     */
    void addTask(int clientId, Runnable task);

    /**
     * Remove all not executed tasks for all client.
     */
    void removeAllTasks();
}
