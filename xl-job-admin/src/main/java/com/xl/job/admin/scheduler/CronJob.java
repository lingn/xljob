package com.xl.job.admin.scheduler;

import com.xl.job.admin.entity.JobInfo;
import com.xl.job.admin.service.JobInfoService;
import com.xl.job.admin.service.JobTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cron 任务执行类
 */
@Slf4j
@Component
public class CronJob implements Job {

    private static JobInfoService jobInfoService;
    private static JobTriggerService jobTriggerService;

    @Autowired
    public void setJobInfoService(JobInfoService jobInfoService) {
        CronJob.jobInfoService = jobInfoService;
    }

    @Autowired
    public void setJobTriggerService(JobTriggerService jobTriggerService) {
        CronJob.jobTriggerService = jobTriggerService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long jobId = dataMap.getLong("jobId");

        log.debug("Cron触发: jobId={}, time={}", jobId, context.getFireTime());

        try {
            JobInfo jobInfo = jobInfoService.getById(jobId);
            if (jobInfo == null) {
                log.warn("任务不存在: jobId={}", jobId);
                return;
            }

            if (jobInfo.getStatus() != 1) {
                log.debug("任务已停止, 跳过执行: jobId={}", jobId);
                return;
            }

            // 触发任务
            jobTriggerService.trigger(jobInfo);

        } catch (Exception e) {
            log.error("任务执行异常: jobId={}, error={}", jobId, e.getMessage(), e);
        }
    }
}
