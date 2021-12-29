package ua.com.solidity.common;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DeferredTasks {
    protected final List<DeferredTask> tasks = new ArrayList<>();
    private final long milliSeconds;
    private long waitingFor;
    private final boolean startupOnly;
    private boolean firstLoopExecuted = false;
    private WaitingThread thread = null;

    private static class WaitingThread extends Thread {
        private final DeferredTasks tasks;

        public WaitingThread(DeferredTasks tasks) {
            this.tasks = tasks;
        }

        private void execute() {
            boolean loopIsActive = true;
            boolean firstExecuted = false;
            synchronized(tasks) {
                tasks.beforeExecutionLoop();
                while (loopIsActive) {
                    DeferredTask task = tasks.tasks.isEmpty() ? null : tasks.tasks.get(0);
                    if (task != null) {
                        try {
                            tasks.doExecuteTask(task);
                        } catch (Exception e) {
                            log.error("Deferred Task Execution error.", e);
                        }
                        firstExecuted = true;
                    }
                    else loopIsActive = false;
                }
                tasks.firstLoopExecuted = firstExecuted;
                tasks.thread = null;
            }
        }

        @Override
        public void run() {
            boolean continueLoop = true;
            while (continueLoop) {
                long sleepTime;
                synchronized (tasks) {
                    sleepTime = tasks.waitingFor - Instant.now().toEpochMilli();
                }
                if (sleepTime > 0) {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        continueLoop = false;
                        interrupt();
                    }
                } else continueLoop = false;
                if (interrupted()) break;
            }

            execute();
        }
    }

    public DeferredTasks(boolean startupOnly, long milliSeconds) {
        this.startupOnly = startupOnly;
        this.milliSeconds = milliSeconds;
    }

    private synchronized void resetTimer() {
        waitingFor = Instant.now().toEpochMilli() + milliSeconds;
        if (thread == null) {
            thread = new WaitingThread(this);
            thread.start();
        }
    }

    public final void waitForExecution() {
        if (thread == null || !thread.isAlive()) return;
        try {
            thread.join();
        } catch (InterruptedException e) {
            thread.interrupt();
        }
    }

    public synchronized void clear() {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        tasks.clear();
    }

    protected synchronized void beforeExecutionLoop() {
        // Nothing yet
    }

    protected synchronized void markTaskCompletion(DeferredTask task) {
        // Nothing yet
    }

    private void doCompletedTask(DeferredTask task) {
        markTaskCompletion(task);
        tasks.remove(task);
    }

    private void doExecuteTask(DeferredTask task) {
        tasks.remove(task);
        executeTask(task);
    }

    protected void executeTask(DeferredTask task) {
        task.execute();
    }

    public final synchronized void append(DeferredTask task) {
        if (startupOnly && firstLoopExecuted) {
            doExecuteTask(task);
            return;
        }
        try {
            for (int i = 0; i < tasks.size(); ++i) {
                DeferredTask registeredTask = tasks.get(i);
                DeferredAction action = registeredTask.compareWith(task);
                switch (action) {
                    case IGNORE:
                        doCompletedTask(task);
                        return;
                    case REPLACE:
                        tasks.set(i, task);
                        doCompletedTask(registeredTask);
                        return;
                    case REMOVE_AND_APPEND:
                        doCompletedTask(registeredTask);
                        tasks.add(task);
                        return;
                    default:
                        break;
                }
            }
            tasks.add(task);
        } finally {
            resetTimer();
        }
    }
}
