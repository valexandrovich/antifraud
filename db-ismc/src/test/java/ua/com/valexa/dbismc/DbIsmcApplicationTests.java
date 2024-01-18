package ua.com.valexa.dbismc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.com.valexa.dbismc.model.sys.StoredJob;
import ua.com.valexa.dbismc.model.sys.StoredStep;
import ua.com.valexa.dbismc.repository.sys.StoredJobRepository;
import ua.com.valexa.dbismc.repository.sys.StoredStepRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
class DbIsmcApplicationTests {

	@Autowired
	StoredJobRepository storedJobRepository;

	@Autowired
	StoredStepRepository storedStepRepository;

	@Test
	void loadSimpleStoredJob() {

		StoredJob sj = new StoredJob();
		sj.setId(1L);
		sj.setName("Банкроти");
		sj.setDescription("Відомості про справи про банкрутство - Державна судова адміністрація України");
		sj = storedJobRepository.save(sj);

		StoredStep ss = new StoredStep();
		ss.setStoredJob(sj);

		Map<String, String> parameters = new HashMap<>();
		parameters.put("packageId", "vidomosti-pro-spravi-pro-bankrutstvo-1");
		parameters.put("sourceName", "govua01");
		parameters.put("retries", "3");

		ss.setParameters(parameters);
		ss.setServiceName("downloader");
		ss.setWorkerName("govua");
		ss.setStepOrder(1);
		ss = storedStepRepository.save(ss);


	}

	@Test
	void tst1(){
		Optional<StoredJob> sj =  storedJobRepository.findById(1L);
		System.out.println(sj);
	}

}
