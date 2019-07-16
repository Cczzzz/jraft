package com.jraft.server.Codec;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.jraft.Message.Message;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.Reader;
import java.net.SocketAddress;
import java.nio.charset.Charset;

/**
 * @author chenchang  客户端编码解码
 * @date 2019/7/16 15:49
 */
public class HttpJsonCodec extends CombinedChannelDuplexHandler<HttpJsonCodec.ResponseToJson, HttpJsonCodec.JsonToRequest> {


    public static class JsonToRequest extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            String json = new Gson().toJson(msg);
            byte[] bytes = json.getBytes(CharsetUtil.UTF_8);
            ByteBuf byteBuf = ctx.alloc().buffer().writeBytes(bytes);
            FullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1,
                    HttpMethod.GET,
                    "/",
                    byteBuf);
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                    bytes.length);
            super.write(ctx, request, promise);
        }
    }
    @ChannelHandler.Sharable
    public static class ResponseToJson extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            FullHttpResponse response = (FullHttpResponse) msg;
            byte[] baty = new byte[response.content().readableBytes()];
            response.content().readBytes(baty);
            Message message = new Gson().fromJson(new String(baty, CharsetUtil.UTF_8), Message.class);
            ctx.fireChannelRead(message);
        }
    }

}
