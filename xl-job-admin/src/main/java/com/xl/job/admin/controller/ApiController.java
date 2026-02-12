package com.xl.job.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xl.job.admin.entity.JobRegistry;
import com.xl.job.admin.mapper.JobRegistryMapper;
import com.xl.job.admin.service.JobLogService;
import com.xl.job.core.protocol.CallbackRequest;
import com.xl.job.core.protocol.RegistryRequest;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 接口 Controller - 供执行器调用
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final JobRegistryMapper jobRegistryMapper;
    private final JobLogService jobLogService;

    @Value("${xl.job.admin.heartbeat-timeout:90}")
    private int heartbeatTimeout;

    /**
     * 执行器注册
     */
    @PostMapping("/registry")
    public TriggerResponse registry(@RequestBody RegistryRequest request) {
        log.info("执行器注册: group={}, key={}, value={}",
                request.getRegistryGroup(), request.getRegistryKey(), request.getRegistryValue());

        // 查询是否已存在
        LambdaQueryWrapper<JobRegistry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobRegistry::getRegistryGroup, request.getRegistryGroup())
                .eq(JobRegistry::getRegistryKey, request.getRegistryKey())
                .eq(JobRegistry::getRegistryValue, request.getRegistryValue());

        JobRegistry registry = jobRegistryMapper.selectOne(wrapper);
        if (registry == null) {
            registry = new JobRegistry();
            registry.setRegistryGroup(request.getRegistryGroup());
            registry.setRegistryKey(request.getRegistryKey());
            registry.setRegistryValue(request.getRegistryValue());
            registry.setUpdateTime(LocalDateTime.now());
            jobRegistryMapper.insert(registry);
        } else {
            registry.setUpdateTime(LocalDateTime.now());
            jobRegistryMapper.updateById(registry);
        }

        return TriggerResponse.success();
    }

    /**
     * 执行器注册移除
     */
    @PostMapping("/registryRemove")
    public TriggerResponse registryRemove(@RequestBody RegistryRequest request) {
        log.info("执行器移除注册: group={}, key={}, value={}",
                request.getRegistryGroup(), request.getRegistryKey(), request.getRegistryValue());

        LambdaQueryWrapper<JobRegistry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobRegistry::getRegistryGroup, request.getRegistryGroup())
                .eq(JobRegistry::getRegistryKey, request.getRegistryKey())
                .eq(JobRegistry::getRegistryValue, request.getRegistryValue());
        jobRegistryMapper.delete(wrapper);

        return TriggerResponse.success();
    }

    /**
     * 任务回调
     */
    @PostMapping("/callback")
    public TriggerResponse callback(@RequestBody List<CallbackRequest> callbackList) {
        for (CallbackRequest callback : callbackList) {
            log.info("任务回调: logId={}, handleCode={}", callback.getLogId(), callback.getHandleCode());
            jobLogService.handleCallback(callback);
        }
        return TriggerResponse.success();
    }

    /**
     * 心跳检测
     */
    @GetMapping("/beat")
    public TriggerResponse beat() {
        return TriggerResponse.success();
    }

    /**
     * 清理过期的执行器注册
     */
    public void cleanRegistry() {
        LocalDateTime timeout = LocalDateTime.now().minusSeconds(heartbeatTimeout);
        LambdaQueryWrapper<JobRegistry> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(JobRegistry::getUpdateTime, timeout);
        int count = jobRegistryMapper.delete(wrapper);
        if (count > 0) {
            log.info("清理过期执行器注册: count={}", count);
        }
    }
}
