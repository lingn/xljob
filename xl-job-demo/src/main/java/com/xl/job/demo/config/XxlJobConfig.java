package com.xl.job.demo.config;

import com.xl.job.executor.config.XxlJobExecutorProperties;
import com.xl.job.executor.server.NettyServer;
import com.xl.job.executor.thread.HeartbeatThread;
import com.xl.job.executor.client.AdminClient;
import com.xl.job.executor.processor.JobProcessorFactory;
import com.xl.job.executor.registry.JobRegistryScanner;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 执行器配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(XxlJobExecutorProperties.class)
public class XxlJobConfig {

    private NettyServer nettyServer;
    private HeartbeatThread heartbeatThread;

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
    public NettyServer nettyServer(XxlJobExecutorProperties properties,
                                   JobProcessorFactory jobProcessorFactory,
                                   AdminClient adminClient) {
        nettyServer = new NettyServer(properties, jobProcessorFactory, adminClient);
        nettyServer.start();
        log.info("执行器 Netty 服务启动: port={}", properties.getPort());
        return nettyServer;
    }

    @Bean
    @ConditionalOnMissingBean
    public HeartbeatThread heartbeatThread(XxlJobExecutorProperties properties,
                                           AdminClient adminClient,
                                           NettyServer nettyServer) {
        heartbeatThread = new HeartbeatThread(properties, adminClient, nettyServer);
        heartbeatThread.start();
        log.info("执行器心跳线程启动: appname={}", properties.getAppname());
        return heartbeatThread;
    }

    @PreDestroy
    public void destroy() {
        if (heartbeatThread != null) {
            heartbeatThread.stop();
        }
        if (nettyServer != null) {
            nettyServer.stop();
        }
        log.info("执行器已关闭");
    }
}
