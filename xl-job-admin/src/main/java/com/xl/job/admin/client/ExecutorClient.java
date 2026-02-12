package com.xl.job.admin.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.xl.job.core.protocol.TriggerRequest;
import com.xl.job.core.protocol.TriggerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 执行器客户端 - 发送调度请求
 */
@Slf4j
@Component
public class ExecutorClient {

    private static final int TIMEOUT = 30000;

    /**
     * 发送触发请求
     *
     * @param address 执行器地址
     * @param request 触发请求
     * @return 响应
     */
    public TriggerResponse trigger(String address, TriggerRequest request) {
        String url = "http://" + address + "/trigger";
        try {
            // 使用 Netty 协议，这里简化为 HTTP
            // 实际应该使用 TCP 连接
            HttpResponse response = HttpRequest.post(url)
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(request))
                    .execute();

            if (!response.isOk()) {
                return TriggerResponse.fail("请求执行器失败: " + response.getStatus());
            }

            return JSONUtil.toBean(response.body(), TriggerResponse.class);
        } catch (Exception e) {
            log.error("触发任务失败: address={}, error={}", address, e.getMessage());
            return TriggerResponse.fail("触发任务异常: " + e.getMessage());
        }
    }

    /**
     * 心跳检测
     *
     * @param address 执行器地址
     * @return 是否存活
     */
    public boolean beat(String address) {
        String url = "http://" + address + "/beat";
        try {
            HttpResponse response = HttpRequest.get(url)
                    .timeout(5000)
                    .execute();
            return response.isOk();
        } catch (Exception e) {
            log.debug("心跳检测失败: address={}, error={}", address, e.getMessage());
            return false;
        }
    }
}
