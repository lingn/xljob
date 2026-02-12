package com.xl.job.core.constants;

/**
 * 系统常量定义
 */
public class Constants {

    private Constants() {}

    /**
     * 通信协议版本
     */
    public static final String PROTOCOL_VERSION = "1.0.0";

    /**
     * HTTP 通信超时时间(毫秒)
     */
    public static final int HTTP_TIMEOUT = 30000;

    /**
     * 心跳间隔(秒)
     */
    public static final int HEARTBEAT_INTERVAL = 30;

    /**
     * 心跳超时(秒) - 超过此时间未收到心跳则认为执行器离线
     */
    public static final int HEARTBEAT_TIMEOUT = 90;

    /**
     * 执行器注册表自动清理间隔(秒)
     */
    public static final int REGISTRY_CLEAN_INTERVAL = 30;

    /**
     * 任务调度结果码
     */
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_FAIL = 500;
    public static final int CODE_TIMEOUT = 504;

    /**
     * 任务状态
     */
    public static final int JOB_STATUS_STOP = 0;
    public static final int JOB_STATUS_RUNNING = 1;

    /**
     * 调度类型
     */
    public static final int SCHEDULE_TYPE_CRON = 1;
    public static final int SCHEDULE_TYPE_FIX_RATE = 2;
    public static final int SCHEDULE_TYPE_FIX_DELAY = 3;

    /**
     * 错过策略
     */
    public static final int MISFIRE_EXECUTE_IMMEDIATELY = 1;
    public static final int MISFIRE_IGNORE = 2;

    /**
     * 地址类型
     */
    public static final int ADDRESS_TYPE_AUTO = 0;
    public static final int ADDRESS_TYPE_MANUAL = 1;

    /**
     * 告警状态
     */
    public static final int ALARM_STATUS_DEFAULT = 0;
    public static final int ALARM_STATUS_NO_NEED = 1;
    public static final int ALARM_STATUS_DONE = 2;
}
