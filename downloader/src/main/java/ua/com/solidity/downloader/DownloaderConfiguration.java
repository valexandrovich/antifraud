package ua.com.solidity.downloader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.solidity.common.ActionObject;
import ua.com.solidity.common.OtpExchange;
import ua.com.solidity.common.RabbitMQListener;

import javax.annotation.PostConstruct;

@Configuration
public class DownloaderConfiguration {
    @Bean
    DownloaderTaskHandler dataGovUa(DataGovUaSourceInfo info) {
        return new DownloaderTaskGovUaHandler(info);
    }

    @Bean
    DownloaderTaskHandler simpleFile(Config config) {
        return new DownloaderFileHandler(config);
    }

    @Bean
    RabbitMQListener listener(Receiver receiver) {
        return new RabbitMQListener(receiver, OtpExchange.DOWNLOADER);
    }

    @PostConstruct
    void initRabbitMQActions() {
        ActionObject.register(DownloaderDropRevisionAction.class, "drop_revision");
        ActionObject.register(DownloaderDropLastRevisionAction.class, "drop_last_revision");
    }
}
