package ua.com.valexa.dbismc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.com.valexa.dbismc.model.DbIsmcApp;
import ua.com.valexa.dbismc.model.sys.StoredJob;
import ua.com.valexa.dbismc.repository.sys.StoredJobRepository;

@SpringBootTest
class DbIsmcApplicationTests {

   @Autowired
    StoredJobRepository storedJobRepository;


    @Test
    void contextLoads() {
        System.out.println("HELLO");

        StoredJob sj = new StoredJob();

    }

}
