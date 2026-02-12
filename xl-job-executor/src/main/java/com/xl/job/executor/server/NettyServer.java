package com.xl.job.executor.server;

import com.xl.job.core.protocol.TriggerRequest;
import com.xl.job.core.protocol.TriggerResponse;
import com.xl.job.executor.client.AdminClient;
import com.xl.job.executor.config.XxlJobExecutorProperties;
import com.xl.job.executor.processor.JobProcessor;
import com.xl.job.executor.processor.JobProcessorFactory;
import com.xl.job.executor.thread.JobThread;
import com.xl.job.executor.thread.TriggerQueue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.json.JSONUtil;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty 服务端 - 接收调度请求
 */
@Slf4j
public class NettyServer {

    private final XxlJobExecutorProperties properties;
    private final JobProcessorFactory jobProcessorFactory;
    private final AdminClient adminClient;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    private final Map<String, JobThread> jobThreadMap = new ConcurrentHashMap<>();
    private final Map<String, TriggerQueue> triggerQueueMap = new ConcurrentHashMap<>();

    public NettyServer(XxlJobExecutorProperties properties, JobProcessorFactory jobProcessorFactory, AdminClient adminClient) {
        this.properties = properties;
        this.jobProcessorFactory = jobProcessorFactory;
        this.adminClient = adminClient;
    }

    /**
     * 启动服务
     */
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 解码器
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                            // 业务处理器
                            pipeline.addLast(new NettyServerHandler(NettyServer.this));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(properties.getPort()).sync();
            channel = future.channel();
            log.info("Netty 服务启动成功, 端口: {}", properties.getPort());
        } catch (Exception e) {
            log.error("Netty 服务启动失败: {}", e.getMessage(), e);
            stop();
        }
    }

    /**
     * 停止服务
     */
    public void stop() {
        // 停止所有任务线程
        jobThreadMap.values().forEach(JobThread::stop);
        jobThreadMap.clear();
        triggerQueueMap.clear();

        // 关闭 Netty
        if (channel != null) {
            channel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty 服务已停止");
    }

    /**
     * 获取执行器地址
     */
    public String getAddress() {
        try {
            String ip = properties.getIp();
            if (ip == null || ip.isEmpty()) {
                ip = InetAddress.getLocalHost().getHostAddress();
            }
            return ip + ":" + properties.getPort();
        } catch (Exception e) {
            log.error("获取执行器地址失败: {}", e.getMessage());
            return "127.0.0.1:" + properties.getPort();
        }
    }

    /**
     * 处理触发请求
     */
    public TriggerResponse handleTrigger(TriggerRequest request) {
        String jobHandler = request.getExecutorHandler();
        JobProcessor jobProcessor = jobProcessorFactory.getJobProcessor(jobHandler);

        if (jobProcessor == null) {
            return TriggerResponse.fail("任务处理器不存在: " + jobHandler);
        }

        // 获取或创建任务队列
        TriggerQueue queue = triggerQueueMap.computeIfAbsent(jobHandler, k -> {
            JobThread jobThread = new JobThread(jobHandler, jobProcessor, adminClient);
            jobThreadMap.put(jobHandler, jobThread);
            TriggerQueue q = new TriggerQueue(jobThread);
            q.start();
            return q;
        });

        return queue.push(request);
    }
}
