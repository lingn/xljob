package com.xl.job.admin.scheduler;

import com.xl.job.admin.entity.JobInfo;
import com.xl.job.admin.service.JobInfoService;
import com.xl.job.admin.service.JobTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 任务调度器 - 基于 Quartz
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduler implements CommandLineRunner {

    private final Scheduler scheduler;
    private final JobInfoService jobInfoService;
    private final JobTriggerService jobTriggerService;

    private final Set<Long> scheduledJobIds = new HashSet<>();

    @Override
    public void run(String... args) {
        try {
            scheduler.start();
            log.info("任务调度器启动成功");

            // 加载所有运行中的任务
            loadRunningJobs();

            // 启动定时刷新任务
            startRefreshThread();
        } catch (SchedulerException e) {
            log.error("任务调度器启动失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 加载所有运行中的任务
     */
    private void loadRunningJobs() {
        List<JobInfo> runningJobs = jobInfoService.listRunningJobs();
        for (JobInfo jobInfo : runningJobs) {
            try {
                scheduleJob(jobInfo);
            } catch (SchedulerException e) {
                log.error("加载任务失败: jobId={}, error={}", jobInfo.getId(), e.getMessage());
            }
        }
        log.info("加载运行中任务完成: count={}", runningJobs.size());
    }

    /**
     * 调度任务
     */
    public void scheduleJob(JobInfo jobInfo) throws SchedulerException {
        // 检查 cron 表达式
        if (jobInfo.getScheduleConf() == null || jobInfo.getScheduleConf().isEmpty()) {
            log.warn("Cron表达式为空: jobId={}", jobInfo.getId());
            return;
        }

        String jobName = String.valueOf(jobInfo.getId());
        String groupName = jobInfo.getJobGroup();

        // 删除已存在的任务
        JobKey jobKey = new JobKey(jobName, groupName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        // 创建 JobDetail
        JobDetail jobDetail = JobBuilder.newJob(CronJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("jobId", jobInfo.getId())
                .build();

        // 创建 Trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName, groupName)
                .withSchedule(CronScheduleBuilder.cronSchedule(jobInfo.getScheduleConf())
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        scheduledJobIds.add(jobInfo.getId());
        log.info("任务调度成功: jobId={}, cron={}", jobInfo.getId(), jobInfo.getScheduleConf());
    }

    /**
     * 取消任务调度
     */
    public void unscheduleJob(JobInfo jobInfo) throws SchedulerException {
        String jobName = String.valueOf(jobInfo.getId());
        String groupName = jobInfo.getJobGroup();
        JobKey jobKey = new JobKey(jobName, groupName);

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            scheduledJobIds.remove(jobInfo.getId());
            log.info("任务取消调度: jobId={}", jobInfo.getId());
        }
    }

    /**
     * 触发任务
     */
    public void triggerJob(JobInfo jobInfo) {
        jobTriggerService.trigger(jobInfo);
    }

    /**
     * 启动定时刷新线程
     */
    private void startRefreshThread() {
        Thread refreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30000); // 30秒刷新一次
                    refreshJobs();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("刷新任务失败: {}", e.getMessage());
                }
            }
        }, "job-refresh-thread");
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * 刷新任务列表
     */
    private void refreshJobs() {
        List<JobInfo> runningJobs = jobInfoService.listRunningJobs();
        Set<Long> currentJobIds = new HashSet<>();

        for (JobInfo jobInfo : runningJobs) {
            currentJobIds.add(jobInfo.getId());
            if (!scheduledJobIds.contains(jobInfo.getId())) {
                try {
                    scheduleJob(jobInfo);
                } catch (SchedulerException e) {
                    log.error("刷新任务失败: jobId={}, error={}", jobInfo.getId(), e.getMessage());
                }
            }
        }

        // 移除已停止的任务
        for (Long jobId : new HashSet<>(scheduledJobIds)) {
            if (!currentJobIds.contains(jobId)) {
                try {
                    JobInfo jobInfo = new JobInfo();
                    jobInfo.setId(jobId);
                    unscheduleJob(jobInfo);
                } catch (SchedulerException e) {
                    log.error("移除任务失败: jobId={}, error={}", jobId, e.getMessage());
                }
            }
        }
    }

    /**
     * 停止调度器
     */
    public void shutdown() {
        try {
            scheduler.shutdown(true);
            log.info("任务调度器已停止");
        } catch (SchedulerException e) {
            log.error("停止调度器失败: {}", e.getMessage());
        }
    }
}
