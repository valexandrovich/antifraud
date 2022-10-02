package ua.com.solidity.statuslogger.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.StatusLogger;
import ua.com.solidity.db.repositories.StatusLoggerRepository;

import java.util.Optional;

@Slf4j
@EnableRabbit
@Component
@RequiredArgsConstructor
@EntityScan(basePackages = {"ua.com.solidity.db.entities"})
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories"})
public class RabbitMQListener {

    @Value("${statuslogger.rabbitmq.name}")
    private String queueName;

    private final StatusLoggerRepository repository;

    @RabbitListener(queues = "${statuslogger.rabbitmq.name}")
    public void processMyQueue(String request) {
        log.debug("Receive from " + queueName + ":\n{}", request);

        ObjectMapper objectMapper = new ObjectMapper();

        StatusLogger statusLogger;
        try {
            statusLogger = objectMapper.readerFor(StatusLogger.class).readValue(request);
            Optional<StatusLogger> statusLoggerRecord = repository.findById(statusLogger.getId());
            if (statusLoggerRecord.isPresent()) {
                log.debug("StatusLogger with revision {} already exist", statusLogger.getId());
                StatusLogger newStatusLogger = statusLoggerRecord.get();
                newStatusLogger.setStarted(statusLogger.getStarted());
                newStatusLogger.setProgress(statusLogger.getProgress());
                newStatusLogger.setUnit(statusLogger.getUnit());
                newStatusLogger.setName(statusLogger.getName());
                newStatusLogger.setUserName(statusLogger.getUserName());
                newStatusLogger.setFinished(statusLogger.getFinished());
                newStatusLogger.setStatus(statusLogger.getStatus().substring(0,
                        Math.min(statusLogger.getStatus().length(), 255)));
                repository.save(newStatusLogger);
                return;
            }

            repository.save(statusLogger);
        } catch (JsonProcessingException e) {
            log.error("Couldn't read object from queue: {}", e.getMessage());
        }
    }
}
