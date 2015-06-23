package shenry.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shenry.workerpool.impl.simple.SimpleTaskExecutor;
import shenry.workerpool.impl.advanced.AdvancedTaskExecutor;

public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    private static class Task implements Runnable {

        private final String id;

        public String getId() {
            return id;
        }

        public Task(String id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                logger.info("Task '{}' started", id);
                Thread.sleep(1000);
                logger.info("Task '{}' finished", id);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.info("Task '{}' interrupted", id);
            } catch (Exception ex) {
                logger.error("Error executing task '{}'", id, ex);
            }
        }

        @Override
        public String toString() {
            return id;
        }
    }

    public static void main(String[] args) throws Exception {

        //TaskExecutor executor = new SimpleTaskExecutor(4);
        TaskExecutor executor = new AdvancedTaskExecutor(4);
        executor.start();
        executor.addTask(1, new Task("1-1"));
        executor.addTask(1, new Task("1-2"));
        executor.addTask(5, new Task("5-1"));
        executor.addTask(5, new Task("5-2"));
        executor.addTask(4, new Task("4-1"));
        executor.addTask(4, new Task("4-2"));
        executor.addTask(1, new Task("1-3"));
        executor.addTask(1, new Task("1-4"));
        executor.addTask(5, new Task("5-3"));
        executor.addTask(1, new Task("1-5"));

        Thread.sleep(15 * 1000);
        executor.stop();
    }
}
