package ua.com.solidity.dwh.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.stereotype.Component;

@Slf4j
@EnableRabbit
@RequiredArgsConstructor
@Component
public class RabbitMqListener {
}
