package com.xl.job.admin.router;

import com.xl.job.core.enums.RouteStrategyEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由策略工厂
 */
public class RouterFactory {

    private static final Map<RouteStrategyEnum, Router> routerMap = new ConcurrentHashMap<>();

    static {
        routerMap.put(RouteStrategyEnum.ROUND, new RoundRouter());
        routerMap.put(RouteStrategyEnum.RANDOM, new RandomRouter());
        routerMap.put(RouteStrategyEnum.FAILOVER, new FailoverRouter());
    }

    public static Router getRouter(RouteStrategyEnum strategy) {
        return routerMap.getOrDefault(strategy, routerMap.get(RouteStrategyEnum.ROUND));
    }
}
