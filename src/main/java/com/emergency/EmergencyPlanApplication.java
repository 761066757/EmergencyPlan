package com.emergency;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.emergency.mapper")
public class EmergencyPlanApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmergencyPlanApplication.class, args);
    }
}
