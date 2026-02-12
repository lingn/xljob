package com.xl.job.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务触发请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long jobId;

    /**
     * 任务Handler名称
     */
    private String executorHandler;

    /**
     * 任务参数
     */
    private String executorParams;

    /**
     * 分片参数 (格式: 分片序号/总分片数)
     */
    private String shardingParam;

    /**
     * 失败重试次数
     */
    private Integer failRetryCount;

    /**
     * 阻塞策略
     */
    private String blockStrategy;

    /**
     * 超时时间(秒)
     */
    private Integer timeout;

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 日志日期时间(用于日志分表)
     */
    private Long logDateTime;
}
