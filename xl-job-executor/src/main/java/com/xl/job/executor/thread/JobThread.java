package com.xl.job.executor.thread;

import com.xl.job.core.protocol.CallbackRequest;
import com.xl.job.core.protocol.TriggerRequest;
import com.xl.job.executor.client.AdminClient;
import com.xl.job.executor.processor.JobProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * 任务执行线程
 */
@Slf4j
public class JobThread {

    private final String jobHandler;
    private final JobProcessor jobProcessor;
    private final AdminClient adminClient;
    private final ExecutorService executorService;

    public JobThread(String jobHandler, JobProcessor jobProcessor, AdminClient adminClient) {
        this.jobHandler = jobHandler;
        this.jobProcessor = jobProcessor;
        this.adminClient = adminClient;
        this.executorService = Executors.newFixedThreadPool(1);
    }

    /**
     * 执行任务
     */
    public void execute(TriggerRequest request) {
        executorService.submit(() -> doExecute(request));
    }

    private void doExecute(TriggerRequest request) {
        long startTime = System.currentTimeMillis();
        String handleMsg;
        int handleCode;

        try {
            log.info("开始执行任务: jobId={}, handler={}", request.getJobId(), request.getExecutorHandler());

            // 执行任务
            String result = jobProcessor.process(request.getExecutorParams());

            handleCode = 200;
            handleMsg = result != null ? result : "执行成功";
            log.info("任务执行完成: jobId={}, result={}, cost={}ms",
                    request.getJobId(), handleMsg, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            handleCode = 500;
            handleMsg = "执行异常: " + e.getMessage();
            log.error("任务执行异常: jobId={}, error={}", request.getJobId(), e.getMessage(), e);
        }

        // 回调执行结果
        callback(request, handleCode, handleMsg);
    }

    private void callback(TriggerRequest request, int handleCode, String handleMsg) {
        try {
            CallbackRequest callback = CallbackRequest.builder()
                    .logId(request.getLogId())
                    .logDateTime(request.getLogDateTime())
                    .handleCode(handleCode)
                    .handleMsg(handleMsg)
                    .build();

            adminClient.callback(Collections.singletonList(callback));
            log.info("任务回调成功: logId={}, handleCode={}", request.getLogId(), handleCode);
        } catch (Exception e) {
            log.error("任务回调失败: logId={}, error={}", request.getLogId(), e.getMessage(), e);
        }
    }

    /**
     * 停止线程
     */
    public void stop() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
