package com.xl.job.executor.thread;

import com.xl.job.core.enums.BlockStrategyEnum;
import com.xl.job.core.protocol.TriggerRequest;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 任务触发队列 - 处理阻塞策略
 */
@Slf4j
public class TriggerQueue {

    private final LinkedBlockingQueue<TriggerRequest> queue = new LinkedBlockingQueue<>(1000);
    private volatile boolean running = false;
    private Thread consumeThread;
    private final JobThread jobThread;

    public TriggerQueue(JobThread jobThread) {
        this.jobThread = jobThread;
    }

    /**
     * 启动队列消费
     */
    public void start() {
        running = true;
        consumeThread = new Thread(() -> {
            while (running) {
                try {
                    TriggerRequest request = queue.poll(3, TimeUnit.SECONDS);
                    if (request != null) {
                        jobThread.execute(request);
                    }
                } catch (InterruptedException e) {
                    log.warn("队列消费线程被中断");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("队列消费异常: {}", e.getMessage(), e);
                }
            }
        }, "trigger-queue-consumer");
        consumeThread.setDaemon(true);
        consumeThread.start();
    }

    /**
     * 停止队列消费
     */
    public void stop() {
        running = false;
        if (consumeThread != null) {
            consumeThread.interrupt();
        }
    }

    /**
     * 添加触发请求
     *
     * @param request 触发请求
     * @return 响应结果
     */
    public TriggerResponse push(TriggerRequest request) {
        BlockStrategyEnum blockStrategy = BlockStrategyEnum.fromCode(request.getBlockStrategy());

        switch (blockStrategy) {
            case SERIAL:
                // 单机串行 - 加入队列等待执行
                if (queue.offer(request)) {
                    return TriggerResponse.success("任务已加入队列");
                } else {
                    return TriggerResponse.fail("队列已满, 任务被丢弃");
                }

            case PARALLEL:
                // 并行执行 - 直接执行不进队列
                jobThread.execute(request);
                return TriggerResponse.success("任务已提交执行");

            case DISCARD:
                // 丢弃后续 - 如果队列不为空则丢弃
                if (queue.isEmpty()) {
                    queue.offer(request);
                    return TriggerResponse.success("任务已加入队列");
                } else {
                    return TriggerResponse.fail("存在运行中的任务, 新任务被丢弃");
                }

            default:
                return TriggerResponse.fail("未知的阻塞策略: " + blockStrategy);
        }
    }

    /**
     * 获取队列大小
     */
    public int size() {
        return queue.size();
    }

    /**
     * 队列是否为空
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
