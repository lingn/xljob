package com.xl.job.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xl.job.admin.entity.JobInfo;
import com.xl.job.admin.entity.JobLog;
import com.xl.job.core.protocol.CallbackRequest;

/**
 * 执行日志服务接口
 */
public interface JobLogService extends IService<JobLog> {

    /**
     * 创建调度日志
     *
     * @param jobInfo 任务信息
     * @return 日志ID
     */
    Long createTriggerLog(JobInfo jobInfo);

    /**
     * 更新调度结果
     *
     * @param logId       日志ID
     * @param triggerCode 调度结果码
     * @param triggerMsg  调度消息
     */
    void updateTriggerResult(Long logId, int triggerCode, String triggerMsg);

    /**
     * 处理回调
     *
     * @param callback 回调请求
     */
    void handleCallback(CallbackRequest callback);

    /**
     * 获取需要重试的日志
     *
     * @return 日志列表
     */
    java.util.List<JobLog> listNeedRetryLogs();
}
