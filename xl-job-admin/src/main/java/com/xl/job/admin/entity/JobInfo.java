package com.xl.job.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务信息实体
 */
@Data
@TableName("xl_job_info")
public class JobInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 执行器组名
     */
    private String jobGroup;

    /**
     * 任务描述
     */
    private String jobDesc;

    /**
     * 调度类型:1-CRON
     */
    private Integer scheduleType;

    /**
     * Cron表达式
     */
    private String scheduleConf;

    /**
     * 错过的策略:1-立即执行,2-忽略
     */
    private Integer misfireStrategy;

    /**
     * 路由策略
     */
    private String executorRouteStrategy;

    /**
     * 任务Handler
     */
    private String executorHandler;

    /**
     * 任务参数
     */
    private String executorParam;

    /**
     * 阻塞策略
     */
    private String executorBlockStrategy;

    /**
     * 超时时间(秒)
     */
    private Integer executorTimeout;

    /**
     * 失败重试次数
     */
    private Integer executorFailRetryCount;

    /**
     * 任务类型
     */
    private String glueType;

    /**
     * 脚本源码
     */
    private String glueSource;

    /**
     * 脚本备注
     */
    private String glueRemark;

    /**
     * 脚本更新时间
     */
    private LocalDateTime glueUpdatetime;

    /**
     * 子任务ID
     */
    private String childJobid;

    /**
     * 状态:0-停止,1-运行
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
