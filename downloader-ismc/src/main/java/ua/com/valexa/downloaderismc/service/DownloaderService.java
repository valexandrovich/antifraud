package ua.com.valexa.downloaderismc.service;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.StepRequestDto;
import ua.com.valexa.downloaderismc.service.govua.GovuaDownloader;

@Service
public class DownloaderService  {
    final ApplicationContext applicationContext;

    //    @Autowired
    private Downloadable downloadable = new GovuaDownloader();

    public DownloaderService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void handleDownload(StepRequestDto dto){
        downloadable = applicationContext.getBean(dto.getWorkerName(), Downloadable.class);
        downloadable.handleDownload(dto.getId(), dto.getParameters());
    }

}
