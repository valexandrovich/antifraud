package ua.com.valexa.schedulerismc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.StepRequestDto;
import ua.com.valexa.common.dto.StepResponseDto;
import ua.com.valexa.common.dto.StoredJobRequestDto;
import ua.com.valexa.dbismc.model.enums.StepStatus;
import ua.com.valexa.dbismc.model.sys.Job;
import ua.com.valexa.dbismc.model.sys.Step;
import ua.com.valexa.dbismc.model.sys.StoredJob;
import ua.com.valexa.dbismc.model.sys.StoredStep;
import ua.com.valexa.dbismc.repository.sys.JobRepository;
import ua.com.valexa.dbismc.repository.sys.StepRepository;
import ua.com.valexa.dbismc.repository.sys.StoredJobRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

@Service
@Slf4j
public class SchedulerService {

    @Autowired
    StoredJobRepository storedJobRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    StepRepository stepRepository;

    public void initStoredJob(StoredJobRequestDto dto) {
        log.debug("Initing Stored job: " + dto.getStoredJobId());
        try {
            StoredJob sj = storedJobRepository.findById(dto.getStoredJobId()).orElseThrow(() -> new RuntimeException("Cant find StoredJob with id: " + dto.getStoredJobId()));

            Job job = new Job();
            job.setStoredJob(sj);
            job.setStartedAt(LocalDateTime.now());
            job.setInitiatorName(dto.getInitiatorName());
            job = jobRepository.save(job);

            StoredStep ss = getFirstStep(sj);
            Step step = new Step();
            step.setJob(job);
            step.setStatus(StepStatus.NEW);
            step.setStartedAt(LocalDateTime.now());
            step.setStoredStep(ss);
            step = stepRepository.save(step);

            StepRequestDto stepRequestDto = new StepRequestDto();
            stepRequestDto.setWorkerName(ss.getWorkerName());
            stepRequestDto.setParameters(ss.getParameters());
            stepRequestDto.setId(step.getId());


            switch (ss.getServiceName()) {
                case "downloader": {
                    log.debug("Sending to : " + "ismc-downloader : "  + stepRequestDto);
                    rabbitTemplate.convertAndSend("ismc-downloader", stepRequestDto);
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    public StoredStep getFirstStep(StoredJob sj) {
        Optional<StoredStep> firstStep = sj.getSteps().stream()
                .filter(StoredStep::getIsEnabled) // filter only enabled steps
                .min(Comparator.comparingInt(StoredStep::getStepOrder)); // find the one with the smallest step order
        if (firstStep.isPresent()) {
            return firstStep.get();
        } else {
            throw new RuntimeException("Cant find first enabled step in StoredJob: " + sj);
        }

    }

    public void handleNextStep(StepResponseDto stepResponseDto) {

        try {
            Step step = stepRepository.findById(stepResponseDto.getStepId()).orElseThrow(() -> new RuntimeException("Can't find Step with id : " + stepResponseDto.getStepId()));
            Job job = step.getJob();
            job.getResults().putAll(stepResponseDto.getResults());
            jobRepository.save(job);





        } catch (Exception e){
            log.error(e.getMessage());
        }



    }
}
