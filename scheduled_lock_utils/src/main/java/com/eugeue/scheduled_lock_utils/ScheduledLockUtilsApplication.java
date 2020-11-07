package com.eugeue.scheduled_lock_utils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationProperties
public class ScheduledLockUtilsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduledLockUtilsApplication.class, args);
    }

}
