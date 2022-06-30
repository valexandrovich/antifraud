package ua.com.solidity.common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import lombok.CustomLog;

import java.nio.charset.StandardCharsets;
import java.util.*;

@CustomLog
public class RabbitMQListener {

    private static class RabbitMQTaskBlank extends RabbitMQTask {

        protected RabbitMQTaskBlank(RabbitMQListener listener, Delivery message) {
            super(false);
            setContext(listener, message, false);
        }

        @Override
        protected DeferredAction compareWith(DeferredTask task) {
            return DeferredAction.APPEND;
        }

        @Override
        protected String description() {
            return "<blank>";
        }

        @Override
        protected void rmqExecute() {
            getListener().handleReceiverMessage(getMessage());
            setAcknowledgeSent(true);
        }
    }

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

        @Override
        public void run() {
            synchronized(listener) {
                while (!isInterrupted()) {
                    DeferredTask task = listener.taskQueue.poll();
                    if (task == null) {
                        setWaiting(true);
                        try {
                            try {
                                log.info("$rmq_listener$>>> before wait");
                                listener.wait();
                                log.info("$rmq_listener$<<< after wait");
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        } finally {
                            setWaiting(false);
                        }
                    } else {
                        log.info("$rmq_listener$=== before exec task");
                        task.execute();
                        log.info("$rmq_listener$=== after exec task");
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

        public synchronized void clearNotAcknowledged() {
            int index = 0;
            while (index < tasks.size()) {
                DeferredTask task = tasks.get(index);
                if (task instanceof RabbitMQTask) {
                    RabbitMQTask rabbitMQTask = (RabbitMQTask) task;
                    if (!rabbitMQTask.isAcknowledgeSent()) {
                        tasks.remove(index);
                        continue;
                    }
                }
                ++index;
            }
        }

        @Override
        public synchronized void clear() {
            tasks.forEach((task) -> {
                if (task instanceof RabbitMQTask) {
                    RabbitMQTask rabbitMQTask = (RabbitMQTask) task;
                    rabbitMQTask.acknowledge(false);
                }
            });
            super.clear();
        }

        @Override
        protected void beforeExecutionLoop(boolean terminated) {
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
    private final Map<String, String> queueTags = new HashMap<>();
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
        RabbitMQListenerThread listenerThread;
        synchronized (this) {
            listenerThread = thread;
            if (tasks != null) tasks.clear();
            while (!taskQueue.isEmpty()) {
                Object obj = taskQueue.remove();
                if (obj instanceof RabbitMQTask) {
                    RabbitMQTask rabbitMQTask = (RabbitMQTask) obj;
                    rabbitMQTask.acknowledge(false);
                }
            }
        }

        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
            try {
                listenerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized(this) {
            thread = null;
        }
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
        if (queueTags.putIfAbsent(queue, null) == null && started) {
            channelNeeded();
        }
    }

    private void clearNotAcknowledged() {
        if (tasks != null) {
            ((InternalDeferredTasks)tasks).clearNotAcknowledged();
        }
        taskQueue.removeIf((task) -> (task instanceof RabbitMQTask) && !((RabbitMQTask) task).isAcknowledgeSent());
    }

    protected final synchronized void cancelChannel() {
        if (channel != null && channel.isOpen()) {
            queueTags.forEach((queue, tag) -> {
                if (tag != null) {
                    try {
                        channel.basicCancel(tag);
                    } catch (Exception e) {
                        log.error("basicCancel with consumerTag {} throws exception.", tag, e);
                    }
                }
            });
            queueTags.replaceAll((k, v) -> null);
        }
        log.info("$rmq_listener$>>> channel cancelled.");
    }

    protected final synchronized void restoreChannel() {
        if (channel != null && channel.isOpen()) {
            for (var entry : queueTags.entrySet()) {
                String queue = entry.getKey();
                String tag = entry.getValue();
                try {
                    if (tag == null && Utils.prepareRabbitMQQueue(queue)) {
                        entry.setValue(channel.basicConsume(queue, false, this::doHandleDeliverCallback, this::doHandleCancelCallback));
                    }
                } catch(Exception e) {
                    log.error("Error on adding queue ({}) to Listener, ignored.", queue, e);
                    channelNeeded();
                }
            }
        } else channelNeeded();
        log.info("$rmq_listener$>>> channel restored.");
    }

    private synchronized boolean channelNeeded() {
        if (!started) return false;
        boolean invalidateQueues = false;
        if (channel == null || !channel.isOpen()) {
            clearNotAcknowledged();
            channel = Utils.createRabbitMQChannel();
            queueTags.replaceAll((k, v) -> null);
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

    private synchronized void doHandleCancelCallback(String consumerTag) {

    }

    private synchronized void doHandleDeliverCallback(String consumerTag, Delivery message) {
        if (thread == null) {
            log.warn("RabbitMQ consumer thread is null");
            return;
        }
        if (thread.isBusy()) {
            log.info("$rmq_listener$^^^^ task unacked {}", new String(message.getBody(), StandardCharsets.UTF_8));
            doAcknowledge(message.getEnvelope().getDeliveryTag(), false); // protection
        } else {
            queueTask(new RabbitMQTaskBlank(this, message));
        }
    }
}
