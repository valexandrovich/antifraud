package ua.com.solidity.common;

import lombok.CustomLog;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@CustomLog
public class DeferredTasks {
    protected final List<DeferrableTask> tasks = new ArrayList<>();
    private final long milliSeconds;
    private long waitingFor;
    private final boolean firstLoopCollectionOnly;
    private boolean firstLoopExecuted = false;
    private WaitingThread thread = null;

    private static class WaitingThread extends Thread {
        private final DeferredTasks tasks;

        public WaitingThread(DeferredTasks tasks) {
            this.tasks = tasks;
        }

        private boolean execute(boolean terminated) {
            List<DeferrableTask> taskList;
            log.info("$deferredTasks$>> wait-thread: before synchronized around tasks.");
            synchronized(tasks) {
                log.info("$deferredTasks$<< wait-thread: inside synchronized around tasks block.");
                tasks.beforeExecutionLoop(terminated);
                if (terminated) return false;
                taskList = new ArrayList<>(tasks.tasks);
                tasks.tasks.clear();
                tasks.waitingFor = 0;
            }

            boolean firstExecuted = !taskList.isEmpty();

            if (!taskList.isEmpty()) {
                log.info("$deferredTasks$-- wait-thread: before execution tasks.");
                tasks.executeTasks(taskList);
            }

            taskList.clear();
            log.info("$deferredTasks$>>  wait-thread: before synchronized around tasks block 2.");
            synchronized(tasks) {
                log.info("$deferredTasks$<< wait-thread: inside synchronized tasks block 2.");
                tasks.firstLoopExecuted = firstExecuted;
                return !tasks.tasks.isEmpty();
            }
        }

        private void loopBody() {
            boolean continueLoop = true;

            while (continueLoop) {
                continueLoop = false;
                long sleepTime;
                synchronized (tasks) {
                    sleepTime = tasks.waitingFor - Instant.now().toEpochMilli();
                }

                if (sleepTime > 0) {
                    try {
                        log.info("$deferredTasks$---- before sleep {}", sleepTime);
                        sleep(sleepTime);
                        log.info("$deferredTasks$---- after sleep {}", sleepTime);
                        continueLoop = true;
                    } catch (InterruptedException e) {
                        log.info("$deferredTasks$---- sleep interruption {}", sleepTime);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        // nothing
                    }
                }
            }
        }

        @Override
        public void run() {
            do {
                loopBody();
            } while (execute(isInterrupted()));

            synchronized(tasks) {
                tasks.thread = null;
            }
        }
    }

    public DeferredTasks(long milliSeconds) {
        this.firstLoopCollectionOnly = false;
        this.milliSeconds = milliSeconds;
    }

    @SuppressWarnings("unused")
    public DeferredTasks(long milliSeconds, boolean firstLoopCollectionOnly) {
        this.firstLoopCollectionOnly = firstLoopCollectionOnly;
        this.milliSeconds = milliSeconds;
    }

    private synchronized void resetTimer() {
        waitingFor = Instant.now().toEpochMilli() + milliSeconds;
        if (thread == null || !thread.isAlive()) {
            thread = new WaitingThread(this);
            thread.start();
        }
    }

    public final void waitForExecution() {
        Thread executionThread;
        synchronized(this) {
            executionThread = thread;
        }
        if (executionThread == null || !executionThread.isAlive()) return;
        try {
            log.info("$deferredTasks$>>> wait for execution...");
            executionThread.join();
            log.info("$deferredTasks$<<< completed...");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // nothing
        }
    }

    public synchronized void clear() {
        tasks.forEach(this::markTaskDeclined);
        tasks.clear();
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        thread = null;
    }

    public synchronized boolean collectionMode() {
        return !(firstLoopExecuted && firstLoopCollectionOnly);
    }

    protected void beforeExecutionLoop(boolean terminated) {
        // Nothing yet
    }

    protected void markTaskDeclined(DeferrableTask task) {
        // nothing yet
    }

    protected void markTaskCompleted(DeferrableTask task) {
        // Nothing yet
    }

    private void doCompletedTask(DeferrableTask task) {
        tasks.remove(task);
        markTaskCompleted(task);
    }

    protected void executeTasks(Collection<? extends DeferrableTask> taskList) {
        log.info("$deferredTasks$ -- inside execute Tasks method.");
        if (taskList != null && !taskList.isEmpty()) {
            taskList.forEach(this::executeTask);
        }
    }

    protected void executeTask(DeferrableTask task) { // overridden in RabbitMQListener.InternalDeferredTasks
        try {
            log.info("$deferredTasks$-- task before execute.");
            task.execute();
            log.info("$deferredTasks$-- task executed.");
        } catch (Exception e) {
            log.error("Deferred Task {} execution error.", task.description(), e);
        }
    }

    public final synchronized boolean append(DeferrableTask task) {
        if (task == null) return false;
        if (!collectionMode() || !task.isDeferred()) {
            executeTask(task);
            return true;
        }

        log.info("$deferred$-- task appended.");
        try {
            for (int i = 0; i < tasks.size(); ++i) {
                DeferrableTask registeredTask = tasks.get(i);
                DeferredAction action = registeredTask.compareWith(task);
                switch (action) {
                    case IGNORE:
                        doCompletedTask(task);
                        return true;
                    case REPLACE:
                        tasks.set(i, task);
                        doCompletedTask(registeredTask);
                        return true;
                    case REMOVE_AND_APPEND:
                        doCompletedTask(registeredTask);
                        tasks.add(task);
                        return true;
                    default:
                        break;
                }
            }
            tasks.add(task);
        } finally {
            resetTimer();
        }
        return true;
    }
}
