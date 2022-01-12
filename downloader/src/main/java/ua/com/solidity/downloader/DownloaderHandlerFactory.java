package ua.com.solidity.downloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DownloaderHandlerFactory {
    private final ApplicationContext context;
    @Autowired
    public DownloaderHandlerFactory(ApplicationContext context) {
        this.context = context;
    }

    public DownloaderTaskHandler getHandler(String name) {
        return context.getBean(name, DownloaderTaskHandler.class);
    }
}
