package com.xl.job.admin.router;

import com.xl.job.admin.client.ExecutorClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 故障转移路由策略
 */
@Slf4j
public class FailoverRouter implements Router {

    private final ExecutorClient executorClient = new ExecutorClient();

    @Override
    public String route(List<String> addressList, Long jobId) {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }

        // 按顺序检测, 返回第一个存活的执行器
        for (String address : addressList) {
            if (beat(address)) {
                return address;
            }
        }

        // 全部不可用, 返回第一个
        log.warn("故障转移路由: 所有执行器都不可用, 返回第一个地址");
        return addressList.get(0);
    }

    /**
     * 心跳检测
     */
    private boolean beat(String address) {
        try {
            return executorClient.beat(address);
        } catch (Exception e) {
            log.warn("心跳检测失败: address={}, error={}", address, e.getMessage());
            return false;
        }
    }
}
