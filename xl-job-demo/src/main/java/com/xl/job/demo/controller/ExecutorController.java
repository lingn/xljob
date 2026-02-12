package com.xl.job.demo.controller;

import com.xl.job.core.protocol.TriggerRequest;
import com.xl.job.core.protocol.TriggerResponse;
import com.xl.job.executor.client.AdminClient;
import com.xl.job.executor.processor.JobProcessor;
import com.xl.job.executor.processor.JobProcessorFactory;
import com.xl.job.executor.thread.JobThread;
import com.xl.job.executor.thread.TriggerQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行器 HTTP 接口 - 接收调度中心的任务触发请求
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ExecutorController {

    private final JobProcessorFactory jobProcessorFactory;
    private final AdminClient adminClient;

    private final Map<String, JobThread> jobThreadMap = new ConcurrentHashMap<>();
    private final Map<String, TriggerQueue> triggerQueueMap = new ConcurrentHashMap<>();

    /**
     * 接收任务触发请求
     */
    @PostMapping("/trigger")
    public TriggerResponse trigger(@RequestBody TriggerRequest request) {
        log.info("收到任务触发请求: jobId={}, handler={}", request.getJobId(), request.getExecutorHandler());

        String jobHandler = request.getExecutorHandler();
        JobProcessor jobProcessor = jobProcessorFactory.getJobProcessor(jobHandler);

        if (jobProcessor == null) {
            log.warn("任务处理器不存在: {}", jobHandler);
            return TriggerResponse.fail("任务处理器不存在: " + jobHandler);
        }

        // 获取或创建任务队列
        TriggerQueue queue = triggerQueueMap.computeIfAbsent(jobHandler, k -> {
            JobThread jobThread = new JobThread(jobHandler, jobProcessor, adminClient);
            jobThreadMap.put(jobHandler, jobThread);
            TriggerQueue q = new TriggerQueue(jobThread);
            q.start();
            return q;
        });

        return queue.push(request);
    }

    /**
     * 心跳检测
     */
    @GetMapping("/beat")
    public TriggerResponse beat() {
        return TriggerResponse.success();
    }

    /**
     * 获取注册的任务列表
     */
    @GetMapping("/jobs")
    public TriggerResponse jobs() {
        return TriggerResponse.success("查询成功", jobProcessorFactory.getAllJobNames());
    }
}
