package ua.com.valexa.downloaderismc.exceptions;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.util.ErrorHandler;

public class QueueExceptionHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable throwable) {
        if (!(throwable instanceof AmqpRejectAndDontRequeueException)) {
            System.out.println("ERROR");
//            throw new AmqpRejectAndDontRequeueException("Error Handler converted exception to fatal", t);
        }
    }
}
