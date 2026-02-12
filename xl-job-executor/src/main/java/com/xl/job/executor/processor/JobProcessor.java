package com.xl.job.executor.processor;

/**
 * 任务处理器接口
 */
public interface JobProcessor {

    /**
     * 执行任务
     *
     * @param param 任务参数
     * @return 执行结果
     * @throws Exception 执行异常
     */
    String process(String param) throws Exception;
}
