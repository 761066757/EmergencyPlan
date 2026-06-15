package com.siasun.iscs.ep;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @description: 启动类
 * @author liao
 * @date 2026/6/15
 * @version 1.0
 */
@EnableAsync
@SpringBootApplication
@MapperScan("com.siasun.iscs.ep.mapper")
public class EmergencyPlanApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmergencyPlanApplication.class, args);
    }
}
