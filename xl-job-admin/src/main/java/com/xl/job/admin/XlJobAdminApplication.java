package com.xl.job.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 调度中心启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.xl.job.admin.mapper")
public class XlJobAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(XlJobAdminApplication.class, args);
    }
}
