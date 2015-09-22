package com.example.ums;

import com.example.billing.Client;
import com.example.billing.Service;
import com.example.email.SendEmail;
import com.example.subscriptions.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.example.billing"})
@EnableCircuitBreaker
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    NamedParameterJdbcTemplate datasource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Client billingclient;

    @Value("${emailQueueName}")
    String emailQueueName;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void run(String... strings) throws Exception {
        logger.info("********Setting up database********");
        jdbcTemplate.execute("DROP TABLE subscriptions IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE subscriptions(" +
                "id SERIAL, userId VARCHAR(255), packageId VARCHAR(255))");
    }

    @Bean
    public SubscriptionRepository subscriptionRepository() {
        return new SubscriptionRepository(datasource);
    }

    @Bean
    public Service billingService(){
        return new Service(billingclient);
    }

    @Bean
    public SendEmail emailSender() {
        return new SendEmail(emailQueueName, rabbitTemplate);
    }
}
