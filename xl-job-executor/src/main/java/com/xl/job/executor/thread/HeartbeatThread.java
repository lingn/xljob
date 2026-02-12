package com.xl.job.executor.thread;

import com.xl.job.executor.client.AdminClient;
import com.xl.job.executor.config.XxlJobExecutorProperties;
import com.xl.job.executor.server.NettyServer;
import com.xl.job.core.protocol.RegistryRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳上报线程
 */
@Slf4j
public class HeartbeatThread {

    private final XxlJobExecutorProperties properties;
    private final AdminClient adminClient;
    private final NettyServer nettyServer;

    private volatile boolean running = false;
    private Thread heartbeatThread;

    public HeartbeatThread(XxlJobExecutorProperties properties, AdminClient adminClient, NettyServer nettyServer) {
        this.properties = properties;
        this.adminClient = adminClient;
        this.nettyServer = nettyServer;
    }

    /**
     * 启动心跳
     */
    public void start() {
        running = true;
        heartbeatThread = new Thread(() -> {
            while (running) {
                try {
                    // 上报心跳
                    String address = nettyServer.getAddress();
                    RegistryRequest request = RegistryRequest.executor(properties.getAppname(), address);
                    adminClient.registry(request);

                    log.debug("心跳上报成功: appname={}, address={}", properties.getAppname(), address);

                    // 休眠
                    Thread.sleep(properties.getHeartbeatInterval() * 1000L);
                } catch (InterruptedException e) {
                    log.info("心跳线程被中断");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("心跳上报失败: {}", e.getMessage());
                    try {
                        Thread.sleep(5000); // 失败后5秒重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "heartbeat-thread");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
        log.info("心跳线程启动, 间隔: {}秒", properties.getHeartbeatInterval());
    }

    /**
     * 停止心跳
     */
    public void stop() {
        running = false;
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }

        // 移除注册
        try {
            String address = nettyServer.getAddress();
            RegistryRequest request = RegistryRequest.executor(properties.getAppname(), address);
            adminClient.registryRemove(request);
            log.info("执行器注册已移除: appname={}, address={}", properties.getAppname(), address);
        } catch (Exception e) {
            log.error("移除执行器注册失败: {}", e.getMessage());
        }
    }
}
