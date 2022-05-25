package ua.com.solidity.common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import lombok.CustomLog;
import java.util.*;


@CustomLog
public class RabbitMQListener {
    private static class RabbitMQListenerThread extends Thread {
        final RabbitMQListener listener;
        private boolean waiting = false;

        public RabbitMQListenerThread(RabbitMQListener listener) {
            this.listener = listener;
        }

        public synchronized boolean isBusy() {
            return !waiting;
        }

        public synchronized void setWaiting(boolean value) {
            if (waiting == value) return;
            waiting = value;
            if (value) {
                listener.restoreChannel();
            } else {
                listener.cancelChannel();
            }
        }

        private DeferredTask getTask() {
            if (listener.taskQueue.isEmpty()) {
                return null;
            } else {
                return listener.taskQueue.remove();
            }
        }

        @Override
        public void run() {
            synchronized(listener) {
                while (!isInterrupted()) {
                    DeferredTask task = getTask();
                    if (task == null) {
                        setWaiting(true);
                        try {
                            try {
                                listener.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        } finally {
                            setWaiting(false);
                        }
                    } else {
                        task.run();
                    }
                }
            }
        }
    }

    private static class InternalDeferredTasks extends DeferredTasks {
        RabbitMQListener listener;
        public InternalDeferredTasks(RabbitMQListener listener, long milliSeconds) {
            super(milliSeconds);
            this.listener = listener;
        }

        @Override
        public synchronized void clear() {
            for (DeferredTask task : tasks) {
                if (task instanceof RabbitMQTask) {
                    RabbitMQTask rabbitMQTask = (RabbitMQTask) task;
                    rabbitMQTask.acknowledge(false);
                }
            }
            super.clear();
        }

        @Override
        protected synchronized void beforeExecutionLoop(boolean terminated) {
            DeferredTask task = tasks.isEmpty() || terminated ? null : tasks.get(0);
            for (int i = terminated ? 0 : 1; i < tasks.size(); ++i) {
                DeferredTask currentTask = tasks.get(i);
                if (currentTask instanceof RabbitMQTask) {
                    RabbitMQTask rabbitMQTask = (RabbitMQTask) currentTask;
                    rabbitMQTask.acknowledge(false);
                }
            }
            tasks.clear();
            if (task != null) {
                tasks.add(task);
            }
        }

        @Override
        protected synchronized void markTaskCompletion(DeferredTask task) {
            if (task instanceof RabbitMQTask) {
                RabbitMQTask rabbitMQTask = (RabbitMQTask) task;
                rabbitMQTask.acknowledge(true);
            }
        }

        @Override
        protected void executeTask(DeferredTask task) {
            listener.queueTask(task);
        }
    }

    private boolean started = false;

    private Channel channel = null;
    private final List<String> consumerTags = new ArrayList<>();
    private final Map<String, Boolean> queues = new HashMap<>();
    private final DeferredTasks tasks;
    private final RabbitMQReceiver receiver;
    private final Queue<DeferredTask> taskQueue = new LinkedList<>();
    private RabbitMQListenerThread thread = null;

    public RabbitMQListener(RabbitMQReceiver receiver, int deferredTimeoutMSecs, String ...queues) {
        this.receiver = receiver;
        for (String queue : queues) {
            addQueue(queue);
        }
        this.tasks = deferredTimeoutMSecs > 0 ? new InternalDeferredTasks(this, deferredTimeoutMSecs) : null;
    }

    public RabbitMQListener(RabbitMQReceiver receiver, String ... queues) {
        this(receiver, 0, queues);
    }

    final synchronized void queueTask(DeferredTask task) {
        taskQueue.add(task);
        notifyAll();
    }

    private void resetThread() {
        synchronized (this) {
            if (tasks != null) tasks.clear();
            while (!taskQueue.isEmpty()) {
                Object obj = taskQueue.remove();
                if (obj instanceof RabbitMQTask) {
                    RabbitMQTask rabbitMQTask = (RabbitMQTask) obj;
                    rabbitMQTask.acknowledge(false);
                }
            }
        }

        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        thread = null;
    }

    public final synchronized boolean start() {
        if (started || receiver == null) return false;
        started = true;
        if (channelNeeded()) {
            if (tasks != null) tasks.clear();
            thread = new RabbitMQListenerThread(this);
            thread.start();
        } else started = false;
        return started;
    }

    @SuppressWarnings("unused")
    public final void finish() {
        if (!started) return;
        resetThread();
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (Exception e) {
                log.error("Can't close RabbitMQ channel.", e);
            }
            channel = null;
            started = false;
        }
    }

    @SuppressWarnings("unused")
    public final void addQueue(String queue) {
        if (queues.putIfAbsent(queue, false) == null && started) {
            channelNeeded();
        }
    }

    protected final synchronized void cancelChannel() {
        if (channel != null && channel.isOpen()) {
            for (String tag : consumerTags) {
                try {
                    channel.basicCancel(tag);
                } catch (Exception e) {
                    log.error("basicCancel with consumerTag {} throws exception.", tag, e);
                }
                queues.replaceAll((k, v) -> false);
            }
            consumerTags.clear();
        }
    }

    protected final synchronized void restoreChannel() {
        if (channel != null && channel.isOpen()) {
            for (var entry : queues.entrySet()) {
                String queue = entry.getKey();
                boolean exists = entry.getValue();
                try {
                    if (!exists && Utils.prepareRabbitMQQueue(queue)) {
                        consumerTags.add(channel.basicConsume(queue, false, this::doHandleDeliverCallback, this::doHandleCancelCallback));
                        queues.put(queue, true);
                    }
                } catch (Exception e) {
                    log.error("Error on adding queue ({}) to Listener, ignored.", queue, e);
                    channel = null;
                }
            }
        } else channelNeeded();
    }

    private synchronized boolean channelNeeded() {
        if (!started) return false;
        boolean invalidateQueues = false;
        if (channel == null || !channel.isOpen()) {
            if (tasks != null) tasks.clear();
            channel = Utils.createRabbitMQChannel();
            queues.replaceAll((k, v) -> false);
            invalidateQueues = channel != null;
        }

        if (invalidateQueues) {
            restoreChannel();
        }
        return channel != null;
    }

    synchronized void doAcknowledge(long deliveryTag, boolean ack) {
        if (channel == null || !channel.isOpen()) return; // nothing to handle, delivery tag must be associated with channel
        try {
            if (ack) {
                channel.basicAck(deliveryTag, false);
            } else {
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (Exception e) {
            log.error("Can't acknowledge a message for rabbitMQ channel", e);
        }
    }

    private void doReceiverAcknowledgeIfNotSent(long deliveryTag, boolean ack) {
        if (!receiver.acknowledgeSent) {
            doAcknowledge(deliveryTag, ack);
        }
    }

    final synchronized void handleReceiverMessage(Delivery message) {
        long deliveryTag = message.getEnvelope().getDeliveryTag();
        Object res = receiver.doHandleMessage(this, message);
        if (res instanceof DeferredTask) {
            DeferredTask task = (DeferredTask) res;
            if (task instanceof RabbitMQTask) {
                RabbitMQTask rabbitMQTask = (RabbitMQTask) task;
                rabbitMQTask.setContext(this, message, receiver.acknowledgeSent);
            } else {
                doReceiverAcknowledgeIfNotSent(deliveryTag, true);
            }
            if (tasks != null) {
                tasks.append(task);
            } else {
                queueTask(task);
            }
        } else {
            doReceiverAcknowledgeIfNotSent(deliveryTag, res == null || !res.equals(false));
        }
    }

    private void doHandleCancelCallback(String consumerTag) {
        // nothing yet
    }

    private synchronized void doHandleDeliverCallback(String consumerTag, Delivery message) {
        if (thread == null) {
            log.warn("RabbitMQ consumer thread is null");
            return;
        }
        if (thread.isBusy()) {
            doAcknowledge(message.getEnvelope().getDeliveryTag(), false); // protection
        } else {
            handleReceiverMessage(message);
        }
    }
}
