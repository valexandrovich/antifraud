package ua.com.solidity.otp.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.com.solidity.otp.web.controllers.HomeController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OTPApplicationTests {

	@Autowired
	private HomeController controller;

	@Test
	void contextLoads() {
		Assertions.assertThat(controller).isNotNull();
	}

}
