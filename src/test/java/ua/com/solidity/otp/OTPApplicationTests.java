package ua.com.solidity.otp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.com.solidity.otp.controllers.HomeController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OTPApplicationTests {

	@Autowired
	private HomeController controller;

	@Test
	void contextLoads() {
		assertThat(controller).isNotNull();
	}

}
