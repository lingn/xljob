package com.xl.job.executor.config;

import com.xl.job.executor.processor.JobProcessorFactory;
import com.xl.job.executor.registry.JobRegistryScanner;
import com.xl.job.executor.server.NettyServer;
import com.xl.job.executor.thread.HeartbeatThread;
import com.xl.job.executor.client.AdminClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 执行器自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(XxlJobExecutorProperties.class)
@ComponentScan(basePackages = "com.xl.job.executor")
public class XxlJobAutoConfiguration {

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

    @Bean
    @ConditionalOnMissingBean
    public NettyServer nettyServer(XxlJobExecutorProperties properties,
                                   JobProcessorFactory jobProcessorFactory,
                                   AdminClient adminClient) {
        return new NettyServer(properties, jobProcessorFactory, adminClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public HeartbeatThread heartbeatThread(XxlJobExecutorProperties properties,
                                           AdminClient adminClient,
                                           NettyServer nettyServer) {
        return new HeartbeatThread(properties, adminClient, nettyServer);
    }
}
