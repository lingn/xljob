package com.xl.job.executor.server;

import cn.hutool.json.JSONUtil;
import com.xl.job.core.protocol.TriggerRequest;
import com.xl.job.core.protocol.TriggerResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty 服务端处理器
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    private final NettyServer nettyServer;

    public NettyServerHandler(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.debug("收到调度请求: {}", msg);

        TriggerResponse response;
        try {
            TriggerRequest request = JSONUtil.toBean(msg, TriggerRequest.class);
            response = nettyServer.handleTrigger(request);
        } catch (Exception e) {
            log.error("处理调度请求异常: {}", e.getMessage(), e);
            response = TriggerResponse.fail("处理请求异常: " + e.getMessage());
        }

        String responseJson = JSONUtil.toJsonStr(response);
        ctx.writeAndFlush(responseJson);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Netty 异常: {}", cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("客户端连接: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客户端断开: {}", ctx.channel().remoteAddress());
    }
}
