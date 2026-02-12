package com.xl.job.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 路由策略枚举
 */
@Getter
@AllArgsConstructor
public enum RouteStrategyEnum {

    ROUND("ROUND", "轮询"),
    RANDOM("RANDOM", "随机"),
    FAILOVER("FAILOVER", "故障转移");

    private final String code;
    private final String desc;

    public static RouteStrategyEnum fromCode(String code) {
        for (RouteStrategyEnum strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return ROUND;
    }
}
