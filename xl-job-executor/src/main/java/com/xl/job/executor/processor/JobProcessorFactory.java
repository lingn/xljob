package com.xl.job.executor.processor;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务处理器工厂
 */
@Slf4j
public class JobProcessorFactory {

    private final Map<String, JobProcessor> jobProcessorMap = new ConcurrentHashMap<>();

    /**
     * 注册任务处理器
     *
     * @param name      处理器名称
     * @param processor 处理器实例
     */
    public void register(String name, JobProcessor processor) {
        log.info("注册任务处理器: {}", name);
        jobProcessorMap.put(name, processor);
    }

    /**
     * 获取任务处理器
     *
     * @param name 处理器名称
     * @return 处理器实例
     */
    public JobProcessor getJobProcessor(String name) {
        return jobProcessorMap.get(name);
    }

    /**
     * 移除任务处理器
     *
     * @param name 处理器名称
     */
    public void remove(String name) {
        log.info("移除任务处理器: {}", name);
        jobProcessorMap.remove(name);
    }

    /**
     * 获取所有任务处理器名称
     */
    public java.util.Set<String> getAllJobNames() {
        return jobProcessorMap.keySet();
    }
}
