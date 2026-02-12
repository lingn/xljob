package com.xl.job.demo;

import com.xl.job.executor.config.XxlJobAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 示例项目启动类
 */
@SpringBootApplication(exclude = {XxlJobAutoConfiguration.class})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
