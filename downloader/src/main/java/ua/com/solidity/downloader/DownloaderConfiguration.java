package ua.com.solidity.downloader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.solidity.common.RabbitMQListener;

@Configuration
public class DownloaderConfiguration {
    @Bean
    DownloaderTaskHandler dataGovUa(DataGovUaSourceInfo info) {
        return new DownloaderTaskGovUaHandler(info);
    }

    @Bean
    DownloaderTaskHandler simpleFile() {
        return new DownloaderFileHandler();
    }

    @Bean
    RabbitMQListener listener(Config config, Receiver receiver) {
        return new RabbitMQListener(receiver, config.getCollectMSecs(), config.getName());
    }
}
