package com.xl.job.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xl.job.admin.entity.JobInfo;
import com.xl.job.admin.entity.JobLog;
import com.xl.job.admin.mapper.JobLogMapper;
import com.xl.job.admin.service.JobLogService;
import com.xl.job.core.protocol.CallbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLog> implements JobLogService {

    @Override
    public Long createTriggerLog(JobInfo jobInfo) {
        JobLog jobLog = new JobLog();
        jobLog.setJobGroup(jobInfo.getJobGroup());
        jobLog.setJobId(jobInfo.getId());
        jobLog.setExecutorHandler(jobInfo.getExecutorHandler());
        jobLog.setExecutorParam(jobInfo.getExecutorParam());
        jobLog.setExecutorFailRetryCount(jobInfo.getExecutorFailRetryCount());
        jobLog.setTriggerTime(LocalDateTime.now());
        jobLog.setAlarmStatus(0);
        save(jobLog);
        return jobLog.getId();
    }

    @Override
    public void updateTriggerResult(Long logId, int triggerCode, String triggerMsg) {
        LambdaUpdateWrapper<JobLog> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(JobLog::getId, logId)
                .set(JobLog::getTriggerCode, triggerCode)
                .set(JobLog::getTriggerMsg, triggerMsg);
        update(wrapper);
    }

    @Override
    public void handleCallback(CallbackRequest callback) {
        LambdaUpdateWrapper<JobLog> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(JobLog::getId, callback.getLogId())
                .set(JobLog::getHandleCode, callback.getHandleCode())
                .set(JobLog::getHandleMsg, callback.getHandleMsg())
                .set(JobLog::getHandleTime, LocalDateTime.now())
                .set(JobLog::getAlarmStatus, callback.getHandleCode() == 200 ? 1 : 0);
        update(wrapper);
        log.info("任务回调处理完成: logId={}, handleCode={}", callback.getLogId(), callback.getHandleCode());
    }

    @Override
    public List<JobLog> listNeedRetryLogs() {
        LambdaQueryWrapper<JobLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobLog::getHandleCode, 500)
                .gt(JobLog::getExecutorFailRetryCount, 0);
        return list(wrapper);
    }
}
