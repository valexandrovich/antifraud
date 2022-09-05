package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import lombok.CustomLog;

import java.nio.charset.StandardCharsets;
import java.util.*;

@CustomLog
public class RabbitMQListener {
    private static class InternalDeferredTasks extends DeferredTasks {
        RabbitMQListener listener;
        public InternalDeferredTasks(RabbitMQListener listener, long milliSeconds) {
            super(milliSeconds);
            this.listener = listener;
        }

        private void clearAllNotEqual(RabbitMQTask ignoredTask) {
            tasks.forEach(task -> {
                if (ignoredTask != task) {
                    RabbitMQTask rabbitMQTask = (RabbitMQTask) task;
                    rabbitMQTask.acknowledge(false);
                }
            });

            tasks.removeIf(task -> task != ignoredTask);
        }

        @Override
        protected void beforeExecutionLoop(boolean terminated) {
            clearAllNotEqual(tasks.isEmpty() || terminated ? null : (RabbitMQTask) tasks.get(0));
        }

        @Override
        protected void markTaskDeclined(DeferrableTask task) {
            RabbitMQTask rabbitMQTask = (RabbitMQTask) task;
            rabbitMQTask.acknowledge(false);
        }

        @Override
        protected void markTaskCompleted(DeferrableTask task) {
            RabbitMQTask rabbitMQTask = (RabbitMQTask) task;
            rabbitMQTask.acknowledge(true);
        }

        @Override
        protected void executeTasks(Collection<? extends DeferrableTask> collection) {
            if (collection == null || collection.isEmpty()) return;
            List<RabbitMQTask> taskList = new ArrayList<>();
            collection.forEach(task-> taskList.add((RabbitMQTask) task)); // must be rabbitMQTask only
            listener.queueTasks(taskList);
        }

        @Override
        protected void executeTask(DeferrableTask task) {
            listener.holdTask((RabbitMQTask) task);
        }
    }

    private boolean started = false;
    private boolean waiting = false;
    private Channel channel = null;
    private final Map<String, String> queueTags = new HashMap<>();
    private final DeferredTasks tasks;
    final RabbitMQReceiver receiver;
    private RabbitMQTask holder = null;

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

    @JsonIgnore
    private synchronized void waitForMessages() {
        waiting = true;
        restoreChannel();
    }

    private void handleHoldenTask() {
        RabbitMQTask task;
        log.info("$rmq$ -- before synchronized block on listener on handleHoldenTask.");
        synchronized(this) {
            log.info("$rmq$ -- inside synchronized block on listener on handleHoldenTask.");
            if (!waiting || holder == null) return;
            task = holder;

            if (tasks != null) {
                tasks.clear();
            }
        }
        log.info("$rmq$ -- before task execution on handleHoldenTask.");
        task.execute();
        log.info("$rmq$ -- after task execution on handleHoldenTask.");
        // use waitForMessages method to return to base mode
        synchronized(this) {
            holder = null;
        }
    }

    final void holdTask(RabbitMQTask task) {
        if (task == null) return;
        log.info("$rmq$ -- before synchronized block on listener (holdTask).");
        synchronized(this) {
            log.info("$rmq$ -- inside synchronized block on listener (holdTask).");
            if (holder != null) {
                task.acknowledge(false); // ignore task
                return;
            } else {
                holder = task;
            }
        }
        log.info("$rmq$ -- before handleHoldenTask on listener (holdTask).");
        handleHoldenTask();
    }

    final void queueTasks(Collection<? extends RabbitMQTask> collection) {
        if (collection == null) return;
        RabbitMQTask task = null;
        for (var item : collection) { // remove all tasks excepts the first one
            if (task == null) {
                task = item;
            } else {
                item.acknowledge(false);
            }
        }

        if (task == null) return;

        synchronized (this) {
            if (holder != null) {
                task.acknowledge(false);
                return;
            }
            holder = task;
        }

        handleHoldenTask();
    }

    public final synchronized boolean start() {
        if (started || receiver == null) return false;
        if (channelNeeded()) {
            if (tasks != null) tasks.clear();
            waitForMessages();
            started = true;
        } else started = false;
        return started;
    }

    @SuppressWarnings("unused")
    public final synchronized void finish() {
        if (!started) return;
        clear();
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
            synchronized(this) {
                channelNeeded();                
            }
        }
    }

    private void clear() {
        if (tasks != null) {
            tasks.clear();
        }
        synchronized(this) {
            if (holder != null) {
                holder.acknowledge(false);
                holder = null;
            }
            cancelChannel();
        }
    }

    private void cancelChannel() {
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
        log.info("$rmq$>>> channel cancelled.");
    }

    private void restoreChannel() {
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
        log.info("$rmq$>>> channel restored.");
    }

    private boolean channelNeeded() {
        boolean invalidateQueues = false;
        if (channel == null || !channel.isOpen()) {
            clear();
            channel = Utils.createRabbitMQChannel();
            if (channel != null) {
                try {
                    channel.basicQos(1);
                } catch (Exception e) {
                    log.error("Can't set a prefetch count for channel.");
                }
            }

            queueTags.replaceAll((k, v) -> null);
            invalidateQueues = channel != null;
        }

        if (invalidateQueues) {
            restoreChannel();
        }
        log.info("$rmq$>>> channel creation result: {}", channel != null);
        return channel != null;
    }

    synchronized void doAcknowledge(long deliveryTag, boolean ack) {
        if (channel == null || !channel.isOpen()) return; // nothing to handle, delivery tag must be associated with channel
        try {
            if (ack) {
                channel.basicAck(deliveryTag, false);
                log.info("$rmq$ --- ack: {}", deliveryTag);
            } else {
                channel.basicNack(deliveryTag, false, true);
                log.info("$rmq$ --- nack: {}", deliveryTag);
            }
        } catch (Exception e) {
            log.error("Can't acknowledge a message for rabbitMQ channel for {}: {}", deliveryTag, ack, e);
        }
    }

    final void handleReceiverMessage(Delivery message) {
        long deliveryTag = message.getEnvelope().getDeliveryTag();
        RabbitMQTask res = receiver.doHandleMessage(this, message);
        if (res != null) {
            res.setContext(this, message);
            if (tasks == null || !res.isDeferred()) {
                log.info("$rmq$::: before hold task.");
                holdTask(res);
            } else {
                log.info("$rmq$::: before append task.");
                tasks.append(res);
            }
        } else { // erroneous situation, that can't be
            log.info("RabbitMQListener ignores a message {}: {}", deliveryTag, new String(message.getBody(), StandardCharsets.UTF_8));
            doAcknowledge(deliveryTag, true);
        }
    }

    private void doHandleCancelCallback(String consumerTag) {
        // nothing yet
    }

    private void doHandleDeliverCallback(String consumerTag, Delivery message) {
        handleReceiverMessage(message);
    }
}
