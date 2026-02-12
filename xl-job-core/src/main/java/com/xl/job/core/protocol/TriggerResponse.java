package com.xl.job.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务触发响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码 (200-成功, 500-失败)
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应内容
     */
    private Object content;

    public static TriggerResponse success() {
        return TriggerResponse.builder()
                .code(200)
                .msg("success")
                .build();
    }

    public static TriggerResponse success(String msg) {
        return TriggerResponse.builder()
                .code(200)
                .msg(msg)
                .build();
    }

    public static TriggerResponse success(String msg, Object content) {
        return TriggerResponse.builder()
                .code(200)
                .msg(msg)
                .content(content)
                .build();
    }

    public static TriggerResponse fail(String msg) {
        return TriggerResponse.builder()
                .code(500)
                .msg(msg)
                .build();
    }

    public static TriggerResponse fail(Integer code, String msg) {
        return TriggerResponse.builder()
                .code(code)
                .msg(msg)
                .build();
    }
}
