package com.xl.job.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行日志实体
 */
@Data
@TableName("xl_job_log")
public class JobLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 执行器组名
     */
    private String jobGroup;

    /**
     * 任务ID
     */
    private Long jobId;

    /**
     * 执行器地址
     */
    private String executorAddress;

    /**
     * 任务Handler
     */
    private String executorHandler;

    /**
     * 任务参数
     */
    private String executorParam;

    /**
     * 分片参数
     */
    private String executorShardingParam;

    /**
     * 失败重试次数
     */
    private Integer executorFailRetryCount;

    /**
     * 调度时间
     */
    private LocalDateTime triggerTime;

    /**
     * 调度结果码
     */
    private Integer triggerCode;

    /**
     * 调度日志
     */
    private String triggerMsg;

    /**
     * 执行时间
     */
    private LocalDateTime handleTime;

    /**
     * 执行结果码
     */
    private Integer handleCode;

    /**
     * 执行日志
     */
    private String handleMsg;

    /**
     * 告警状态:0-默认,1-无需告警,2-已告警
     */
    private Integer alarmStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
