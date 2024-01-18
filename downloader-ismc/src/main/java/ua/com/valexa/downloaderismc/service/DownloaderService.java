package ua.com.valexa.downloaderismc.service;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.StepRequestDto;
import ua.com.valexa.common.dto.StepResponseDto;
import ua.com.valexa.dbismc.model.enums.StepStatus;
import ua.com.valexa.downloaderismc.service.govua.GovuaDownloader;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

@Service
public class DownloaderService {



    ExecutorService executorService = Executors.newFixedThreadPool(2);
    final ApplicationContext applicationContext;

    //    @Autowired
    private Downloadable downloadable = new GovuaDownloader();

    public DownloaderService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public CompletableFuture<StepResponseDto> handleDownload(StepRequestDto stepRequestDto) {



        return CompletableFuture.supplyAsync(() -> downloadable.handleDownload(stepRequestDto.getId(), stepRequestDto.getParameters()), executorService);
    }

}
