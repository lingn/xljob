package com.xl.job.executor.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.xl.job.core.protocol.CallbackRequest;
import com.xl.job.core.protocol.RegistryRequest;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 调度中心客户端
 */
@Slf4j
public class AdminClient {

    private final String adminAddresses;
    private static final int TIMEOUT = 30000;

    public AdminClient(String adminAddresses) {
        this.adminAddresses = adminAddresses;
    }

    /**
     * 注册执行器
     */
    public TriggerResponse registry(RegistryRequest request) {
        return post("/api/registry", request);
    }

    /**
     * 移除执行器注册
     */
    public TriggerResponse registryRemove(RegistryRequest request) {
        return post("/api/registryRemove", request);
    }

    /**
     * 回调任务执行结果
     */
    public TriggerResponse callback(List<CallbackRequest> callbackList) {
        return post("/api/callback", callbackList);
    }

    private TriggerResponse post(String path, Object body) {
        String url = adminAddresses + path;
        try {
            HttpResponse response = HttpRequest.post(url)
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(body))
                    .execute();

            if (!response.isOk()) {
                log.error("请求调度中心失败: status={}, body={}", response.getStatus(), response.body());
                return TriggerResponse.fail("请求调度中心失败: " + response.getStatus());
            }

            String bodyStr = response.body();
            return JSONUtil.toBean(bodyStr, TriggerResponse.class);
        } catch (Exception e) {
            log.error("请求调度中心异常: {}", e.getMessage(), e);
            return TriggerResponse.fail("请求调度中心异常: " + e.getMessage());
        }
    }
}
