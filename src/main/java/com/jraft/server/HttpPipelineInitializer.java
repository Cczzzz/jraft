package com.jraft.server;

import com.jraft.server.Codec.HttpJsonCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;

import java.util.List;


/**
 * @author chenchang
 * @date 2019/6/25 21:23
 * http编码解码器
 */
public class HttpPipelineInitializer extends ChannelInitializer<Channel> {
    private boolean isClient;
    private ChannelHandler[] handlers;


    public HttpPipelineInitializer(boolean isClient, ChannelHandler... handlers) {
        this.isClient = isClient;
        this.handlers = handlers;
    }


    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (isClient) {
            //包含编码器和解码器
            pipeline.addLast(new HttpClientCodec());
            //聚合
            pipeline.addLast(new HttpObjectAggregator(1024 * 10 * 1024));
            //解压
            pipeline.addLast(new HttpContentDecompressor());
            pipeline.addLast(new HttpJsonCodec.JsonToRequest());
            pipeline.addLast(new HttpJsonCodec.ResponseToJson());
            pipeline.addLast(handlers);
        } else {
            //包含编码器和解码器
            pipeline.addLast(new HttpServerCodec());
            //聚合
            pipeline.addLast(new HttpObjectAggregator(1024 * 10 * 1024));
            //压缩
            pipeline.addLast(new HttpContentCompressor());
            pipeline.addLast(handlers);
        }


    }
}
