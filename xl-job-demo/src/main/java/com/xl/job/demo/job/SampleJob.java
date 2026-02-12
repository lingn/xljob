package com.xl.job.demo.job;

import com.xl.job.executor.annotations.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 示例任务
 */
@Slf4j
@Component
public class SampleJob {

    /**
     * 简单任务
     */
    @XxlJob("demoJob")
    public void demoJob() {
        log.info("执行 demoJob: {}", LocalDateTime.now());
    }

    /**
     * 带参数的任务
     */
    @XxlJob("demoJobWithParam")
    public String demoJobWithParam(String param) {
        log.info("执行 demoJobWithParam, 参数: {}", param);
        return "执行成功, 参数: " + param;
    }

    /**
     * 耗时任务
     */
    @XxlJob("longRunningJob")
    public void longRunningJob() throws InterruptedException {
        log.info("开始执行耗时任务...");
        for (int i = 1; i <= 5; i++) {
            TimeUnit.SECONDS.sleep(1);
            log.info("耗时任务进度: {}/5", i);
        }
        log.info("耗时任务执行完成");
    }

    /**
     * 可能失败的任务(模拟失败场景)
     */
    @XxlJob("mightFailJob")
    public void mightFailJob() {
        double random = Math.random();
        log.info("执行 mightFailJob, random={}", random);

        if (random > 0.5) {
            throw new RuntimeException("模拟任务执行失败");
        }

        log.info("mightFailJob 执行成功");
    }
}
