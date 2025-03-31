package com.bbekesi.interview.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
public class InstantPaymentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstantPaymentApiApplication.class, args);
    }

}
