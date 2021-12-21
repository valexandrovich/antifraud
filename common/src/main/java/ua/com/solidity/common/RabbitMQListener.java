package ua.com.solidity.common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RabbitMQListener {

    private static class InternalDeferredTasks extends DeferredTasks {
        RabbitMQListener listener;
        public InternalDeferredTasks(RabbitMQListener listener, boolean startupOnly, long milliSeconds) {
            super(startupOnly, milliSeconds);
            this.listener = listener;
        }

        @Override
        protected synchronized void beforeExecutionLoop() {
            DeferredTask task = tasks.isEmpty() ? null : tasks.get(0);

            for (int i = 1; i < tasks.size(); ++i) {
                markTaskCompletion(tasks.get(i), false);
            }

            tasks.clear();
            if (task != null) {
                tasks.add(task);
            }
        }

        @Override
        protected synchronized void markTaskCompletion(DeferredTask task, boolean value) {
            listener.doAcknowledge(task.getTag(), value);
        }
    }

    private boolean started = false;

    private Channel channel = null;
    private final Map<String, Boolean> queues = new HashMap<>();
    private DeferredTasks tasks = null;
    private boolean startupOnly;
    private int deferredTimeoutMSecs;
    private RabbitMQReceiver receiver;

    public RabbitMQListener(RabbitMQReceiver receiver, boolean deferredFirstLoopOnly, int deferredTimeoutMSecs, String ...queues) {
        initialize(receiver, deferredFirstLoopOnly, deferredTimeoutMSecs, queues);
    }

    public RabbitMQListener(RabbitMQReceiver receiver, String ... queues) {
        initialize(receiver, true, 0, queues);
    }

    protected final void initialize(RabbitMQReceiver receiver, boolean startupOnly, int deferredTimeoutMSecs, String[] queues) {
        this.receiver = receiver;
        for (String queue : queues) {
            doAddQueue(queue, false);
        }
        this.startupOnly = startupOnly;
        this.deferredTimeoutMSecs = deferredTimeoutMSecs;
    }

    public final void start() {
        if (started || receiver == null) return;
        started = true;
        channelNeeded();
        if (deferredTimeoutMSecs > 0) {
            deferredTimeoutMSecs = Math.max(deferredTimeoutMSecs, 100);
            tasks = new InternalDeferredTasks(this, startupOnly, deferredTimeoutMSecs);
        }
    }

    @SuppressWarnings("unused")
    public final void finish() {
        if (!started) return;
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

    private void doAddQueue(String queue, boolean immediate) {
        queues.putIfAbsent(queue, false);
        if (immediate && started) channelNeeded();
    }

    @SuppressWarnings("unused")
    public final void addQueue(String queue) {
        doAddQueue(queue, true);
    }

    private void channelNeeded() {
        if (!started) return;
        if (channel == null || ! channel.isOpen()) {
            if (tasks != null) tasks.clear();
            channel = null;
            channel = Utils.createRabbitMQChannel();
            queues.replaceAll((k, v) -> false);
        }

        for (String queue : queues.keySet()) {
            try {
                if (Utils.prepareRabbitMQQueue(queue)) {
                    channel.basicConsume(queue, false, this::doHandleDeliverCallback, this::doHandleCancelCallback);
                    queues.put(queue, true);
                }
            } catch (Exception e) {
                log.error("Error on adding queue ({}) to Listener, ignored.", queue, e);
            }
        }
    }

    private synchronized void doAcknowledge(long deliveryTag, boolean ack) {
        channelNeeded();
        if (channel != null) {
            try {
                if (ack) {
                    channel.basicAck(deliveryTag, false);
                } else {
                    channel.basicNack(deliveryTag, false, true);
                }
            } catch (Exception e) {
                log.error("Can't acknowledge a message for rabbitMQ channel", e);
                channelNeeded();
            }
        }
    }

    private void doHandleCancelCallback(String consumerTag) {
        // nothing yet
    }

    private void doHandleDeliverCallback(String consumerTag, Delivery message) {
        boolean handled;
        DeferredTask task;
        String queue = message.getEnvelope().getRoutingKey();
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            Object res = receiver == null ? null : receiver.handleMessage(queue, msg);
            if (res instanceof DeferredTask) {
                task = (DeferredTask) res;
                if (tasks != null) {
                    task.setTag(message.getEnvelope().getDeliveryTag());
                    tasks.append(task);
                    return;
                } else {
                    handled = task.execute();
                }
            } else handled = res != null && !res.equals(false);
        } catch (Exception e) {
            handled = true;
        }
        doAcknowledge(message.getEnvelope().getDeliveryTag(), handled);
    }
}
