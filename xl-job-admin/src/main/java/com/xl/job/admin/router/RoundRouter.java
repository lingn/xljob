package com.xl.job.admin.router;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询路由策略
 */
public class RoundRouter implements Router {

    private final ConcurrentHashMap<Long, AtomicInteger> routeCountMap = new ConcurrentHashMap<>();

    @Override
    public String route(List<String> addressList, Long jobId) {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }

        AtomicInteger counter = routeCountMap.computeIfAbsent(jobId, k -> new AtomicInteger(0));
        int index = Math.abs(counter.getAndIncrement()) % addressList.size();
        return addressList.get(index);
    }
}
