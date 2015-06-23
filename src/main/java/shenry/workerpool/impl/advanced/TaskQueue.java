package shenry.workerpool.impl.advanced;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import shenry.workerpool.impl.ClientTask;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

/**
 * A queue for worker tasks. Stores current executed clients ids in a set.
 * When task is added to the queue, it is wrapped by other Runnable, that
 * removes tasks's clientId from currently executed clients set after task
 * is executed.
 *
 * This class is thread safe.
 */
@ThreadSafe
class TaskQueue {
    private final Object lock = new Object();

    /**
     * Contains clientIds of currently executed tasks. Only one
     * task of each client can be executed at the same time.
     */
    @GuardedBy("lock")
    private Set<Integer> executingClients = new HashSet<Integer>();

    /**
     *
     */
    @GuardedBy("lock")
    private LinkedList<ClientTask> clientTasks = new LinkedList<ClientTask>();

    /**
     * Add client task to queue. When task is added to the queue, it is wrapped
     * by other Runnable, that removes tasks's clientId from currently executed
     * clients set after task is executed.
     *
     * @param clientId clientId of task
     * @param task task for execution
     */
    public void add(final int clientId, final Runnable task) {
        if (task != null) {
            synchronized (lock) {
                Runnable taskWrapper = new Runnable() {
                    @Override
                    public void run() {
                        task.run();

                        synchronized (lock) {
                            executingClients.remove(clientId);
                        }
                    }
                };

                clientTasks.addLast(new ClientTask(clientId, taskWrapper));
            }
        }
    }

    /**
     * Return task for execution and add clientId of task in currently executed
     * clients set. If all clients are being processed, returns NULL.
     *
     * @return task for execution or NULL.
     */
    public Runnable nextTask() {
        Runnable task = null;

        synchronized (lock) {
            ListIterator<ClientTask> it = clientTasks.listIterator();
            while (it.hasNext()) {
                ClientTask clientTask = it.next();

                if (!executingClients.contains(clientTask.getClientId())) {
                    it.remove();
                    executingClients.add(clientTask.getClientId());
                    task = clientTask.getTask();
                    break;
                }
            }
        }

        return task;
    }

    /**
     * Remove all tasks from queue.
     */
    public void clear() {
        synchronized (lock) {
            clientTasks.clear();
        }
    }
}
