package ua.com.solidity.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RabbitMQTask extends DeferredTask {
    private RabbitMQListener listener;
    private long deliveryTag;
    private final boolean autoAck;
    private final boolean inThread;
    private boolean acknowledgeSent = false;
    private RabbitMQExecutionThread thread = null;

    private static class RabbitMQExecutionThread extends Thread {
        private final RabbitMQTask task;
        public RabbitMQExecutionThread(RabbitMQTask task) {
            this.task = task;
        }
        @Override
        public void run() {
            task.internalExecute();
        }
    }

    protected RabbitMQTask(boolean autoAck, boolean inThread) {
        this.autoAck = autoAck;
        this.inThread = inThread;
    }

    public final void reset() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null;
        }
    }

    final synchronized void setContext(RabbitMQListener listener, long deliveryTag) {
        this.listener = listener;
        this.deliveryTag = deliveryTag;
    }

    private synchronized void internalExecute() {
        try {
            execute();
        } catch (Exception e) {
            log.error("RabbitMQTask execution error.", e);
        } finally {
            if (!acknowledgeSent) acknowledge(true);
            thread = null;
            listener.unlock();
            listener = null;
            deliveryTag = 0;
        }
    }

    final void rabbitMQExecute() {
        if (listener != null) {
            this.listener.lock();
            if (autoAck) {
                listener.doAcknowledge(deliveryTag, true);
                acknowledgeSent = true;
            }

            if (inThread) {
                thread = new RabbitMQExecutionThread(this);
                thread.start();
            } else internalExecute();
        }
    }

    protected final void acknowledge(boolean ack) {
        if (listener != null && !acknowledgeSent) {
            listener.doAcknowledge(deliveryTag, ack);
            acknowledgeSent = true;
        }
    }

    protected final void send(String queue, String message) {
        Utils.sendRabbitMQMessage(queue, message);
    }
}
