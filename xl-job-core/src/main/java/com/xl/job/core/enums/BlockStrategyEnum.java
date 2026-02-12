package com.xl.job.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 阻塞策略枚举
 */
@Getter
@AllArgsConstructor
public enum BlockStrategyEnum {

    SERIAL("SERIAL", "单机串行"),
    PARALLEL("PARALLEL", "并行执行"),
    DISCARD("DISCARD", "丢弃后续");

    private final String code;
    private final String desc;

    public static BlockStrategyEnum fromCode(String code) {
        for (BlockStrategyEnum strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return SERIAL;
    }
}
