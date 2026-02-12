package com.xl.job.executor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 执行器配置属性
 */
@Data
@ConfigurationProperties(prefix = "xl.job.executor")
public class XxlJobExecutorProperties {

    /**
     * 执行器AppName
     */
    private String appname = "xl-job-executor";

    /**
     * 执行器标题
     */
    private String title = "默认执行器";

    /**
     * 执行器端口
     */
    private int port = 9999;

    /**
     * 调度中心地址
     */
    private String adminAddresses = "http://127.0.0.1:8080";

    /**
     * 执行器IP (为空则自动获取)
     */
    private String ip;

    /**
     * 日志路径
     */
    private String logPath = "/data/applogs/xl-job/jobhandler";

    /**
     * 日志保留天数
     */
    private int logRetentionDays = 30;

    /**
     * 心跳间隔(秒)
     */
    private int heartbeatInterval = 30;
}
