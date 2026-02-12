package com.xl.job.demo.config;

import com.xl.job.core.protocol.RegistryRequest;
import com.xl.job.executor.client.AdminClient;
import com.xl.job.executor.config.XxlJobExecutorProperties;
import com.xl.job.executor.processor.JobProcessorFactory;
import com.xl.job.executor.registry.JobRegistryScanner;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * 执行器配置 - 简化版，使用 HTTP 协议
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(XxlJobExecutorProperties.class)
public class XxlJobConfig {

    @Value("${server.port:8080}")
    private int httpPort;

    private Thread heartbeatThread;
    private volatile boolean running = true;

    @Bean
    @ConditionalOnMissingBean
    public AdminClient adminClient(XxlJobExecutorProperties properties) {
        return new AdminClient(properties.getAdminAddresses());
    }

    @Bean
    @ConditionalOnMissingBean
    public JobProcessorFactory jobProcessorFactory() {
        return new JobProcessorFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public JobRegistryScanner jobRegistryScanner(JobProcessorFactory jobProcessorFactory) {
        return new JobRegistryScanner(jobProcessorFactory);
    }

    /**
     * 启动心跳线程，注册 HTTP 地址
     */
    @Bean
    public Object heartbeatStarter(XxlJobExecutorProperties properties, AdminClient adminClient) {
        heartbeatThread = new Thread(() -> {
            while (running) {
                try {
                    // 获取本机 IP
                    String ip = properties.getIp();
                    if (ip == null || ip.isEmpty()) {
                        ip = InetAddress.getLocalHost().getHostAddress();
                    }
                    // 注册 HTTP 端口（Spring Boot 的 server.port）
                    String address = ip + ":" + httpPort;

                    RegistryRequest request = RegistryRequest.executor(properties.getAppname(), address);
                    adminClient.registry(request);
                    log.debug("心跳注册成功: appname={}, address={}", properties.getAppname(), address);

                    Thread.sleep(properties.getHeartbeatInterval() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("心跳注册失败: {}", e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "heartbeat-thread");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
        log.info("执行器心跳线程启动: appname={}, httpPort={}", properties.getAppname(), httpPort);

        return new Object();
    }

    @PreDestroy
    public void destroy() {
        running = false;
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }
        log.info("执行器已关闭");
    }
}
