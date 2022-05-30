package ua.com.solidity.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Getter
@Setter
@Component
@CustomLog
public class Config {
    @Value("${scheduler.rabbitmq.name}")
    private String name;
    @Value("${scheduler.rabbitmq.init}")
    private String initName;
    @Value("${scheduler.init}")
    private String init;
    @Value("${scheduler.schedulerInitFile}")
    private String schedulerInitFile;
    @Value("${scheduler.locale}")
    private String schedulerLocaleName;

    @Getter(AccessLevel.NONE)
    private Locale locale;

    @SuppressWarnings("unused")
    @JsonIgnore
    public Locale getSchedulerLocale() {
        if (locale == null) {
            if (schedulerLocaleName.equals("default")) {
                locale = Locale.getDefault();
            } else {
                try {
                    locale = new Locale(schedulerLocaleName);
                } catch (Exception e) {
                    log.warn("Locale \"{}\" not found, default locale used.", schedulerLocaleName);
                }
            }
        }
        return locale;
    }
}
