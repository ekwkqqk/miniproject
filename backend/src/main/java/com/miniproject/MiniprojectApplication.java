package com.miniproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.miniproject")
@ConfigurationPropertiesScan("com.miniproject")
@EnableScheduling
public class MiniprojectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniprojectApplication.class, args);
    }
}
