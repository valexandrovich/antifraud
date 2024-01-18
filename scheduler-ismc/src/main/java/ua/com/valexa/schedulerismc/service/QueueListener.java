package ua.com.valexa.schedulerismc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.StepResponseDto;
import ua.com.valexa.common.dto.StoredJobRequestDto;

@Service
@Slf4j
public class QueueListener {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SchedulerService service;


    @RabbitListener(queues = "ismc-scheduler-init", errorHandler = "queueListenerErrorHandler")
    public void receiveInitMessage(StoredJobRequestDto storedJobRequestDto) {
        log.debug("Scheduler get message: " + storedJobRequestDto.toString().replaceAll("\n", " ").replaceAll("\r", " "));
        try {
            service.initStoredJob(storedJobRequestDto);
        } catch (Exception e){
            // TODO catch errors from executor
            log.error(e.getMessage());
        }
    }

    @RabbitListener(queues = "ismc-scheduler-response", errorHandler = "queueListenerErrorHandler")
    public void receiveResponseMessage(StepResponseDto stepResponseDto) {
        log.debug("Scheduler got StepResponseDto: " + stepResponseDto);
        service.handleNextStep(stepResponseDto);
//        System.out.println(stepResponseDto);
//        log.debug("Scheduler get message: " + storedJobRequestDto.toString().replaceAll("\n", " ").replaceAll("\r", " "));
//        try {
//            service.initStoredJob(storedJobRequestDto);
//        } catch (Exception e){
//             TODO catch errors from executor
//            log.error(e.getMessage());
//        }
    }
}
