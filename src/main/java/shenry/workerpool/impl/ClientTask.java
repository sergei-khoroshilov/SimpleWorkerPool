package shenry.workerpool.impl;

public class ClientTask {
    private int clientId;
    private Runnable task;

    public ClientTask(int clientId, Runnable task) {
        this.clientId = clientId;
        this.task = task;
    }

    public int getClientId() {
        return clientId;
    }

    public Runnable getTask() {
        return task;
    }
}
