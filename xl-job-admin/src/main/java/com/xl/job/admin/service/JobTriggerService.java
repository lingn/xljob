package com.xl.job.admin.service;

import com.xl.job.admin.entity.JobInfo;

/**
 * 任务触发服务接口
 */
public interface JobTriggerService {

    /**
     * 触发任务
     *
     * @param jobId 任务ID
     */
    void trigger(Long jobId);

    /**
     * 触发任务(带参数)
     *
     * @param jobId       任务ID
     * @param executorParam 任务参数
     * @param failRetryCount 失败重试次数
     */
    void trigger(Long jobId, String executorParam, Integer failRetryCount);

    /**
     * 直接触发任务
     *
     * @param jobInfo 任务信息
     */
    void trigger(JobInfo jobInfo);
}
