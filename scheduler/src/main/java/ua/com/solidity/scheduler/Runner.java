package ua.com.solidity.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Runner implements CommandLineRunner {

    private final Scheduler scheduler;

    @Autowired
    public Runner(Scheduler mainScheduler) {
        this.scheduler = mainScheduler;
    }

    @Override
    public void run(String... args) {
        scheduler.refresh();
    }
}
