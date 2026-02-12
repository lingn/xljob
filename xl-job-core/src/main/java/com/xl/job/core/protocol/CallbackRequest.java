package com.xl.job.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务执行回调请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallbackRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 日志日期时间
     */
    private Long logDateTime;

    /**
     * 执行结果码 (200-成功, 500-失败)
     */
    private Integer handleCode;

    /**
     * 执行结果消息
     */
    private String handleMsg;

    public static CallbackRequest success(Long logId, Long logDateTime, String msg) {
        return CallbackRequest.builder()
                .logId(logId)
                .logDateTime(logDateTime)
                .handleCode(200)
                .handleMsg(msg)
                .build();
    }

    public static CallbackRequest fail(Long logId, Long logDateTime, String msg) {
        return CallbackRequest.builder()
                .logId(logId)
                .logDateTime(logDateTime)
                .handleCode(500)
                .handleMsg(msg)
                .build();
    }
}
