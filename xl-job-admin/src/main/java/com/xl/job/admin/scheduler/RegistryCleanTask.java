package com.xl.job.admin.scheduler;

import com.xl.job.admin.controller.ApiController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时清理过期执行器注册
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegistryCleanTask {

    private final ApiController apiController;

    /**
     * 每30秒清理一次过期注册
     */
    @Scheduled(fixedRate = 30000)
    public void cleanRegistry() {
        try {
            apiController.cleanRegistry();
        } catch (Exception e) {
            log.error("清理注册表失败: {}", e.getMessage());
        }
    }
}
