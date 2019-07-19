package com.jraft.server.Codec;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.jraft.Message.Message;
import com.jraft.Message.MsgType;
import com.jraft.Message.MsgVote;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.time.Year;

/**
 * @author chenchang  json编码解码
 * @date 2019/7/16 15:49
 */
public class HttpJsonCodec extends CombinedChannelDuplexHandler<HttpJsonCodec.ResponseToJson, HttpJsonCodec.JsonToRequest> {

    private static final String HeaderMsgType = "HeaderMsgType";

    public static class JsonToRequest extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            Message message = (Message) msg;
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
            request.headers().set(HeaderMsgType, message.getType());
            super.write(ctx, request, promise);
        }
    }

    @ChannelHandler.Sharable
    public static class ResponseToJson extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            FullHttpResponse response = (FullHttpResponse) msg;
            byte[] bytes = new byte[response.content().readableBytes()];
            response.content().readBytes(bytes);
            int msgType = response.headers().getInt(HeaderMsgType);
            Message message = Decoder(bytes, msgType);
            ctx.fireChannelRead(message);
        }
    }

    public static class JsonToResponse extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            Message message = (Message) msg;
            String json = new Gson().toJson(msg);
            byte[] bytes = json.getBytes(CharsetUtil.UTF_8);
            ByteBuf byteBuf = ctx.alloc().buffer().writeBytes(bytes);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    byteBuf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                    bytes.length);
            response.headers().set(HeaderMsgType, message.getType());
            super.write(ctx, response, promise);
        }
    }

    @ChannelHandler.Sharable
    public static class RequestToJson extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            FullHttpRequest request = (FullHttpRequest) msg;
            byte[] bytes = new byte[request.content().readableBytes()];
            request.content().readBytes(bytes);
            int msgType = request.headers().getInt(HeaderMsgType);
            Message message = Decoder(bytes, msgType);
            ctx.fireChannelRead(message);
        }
    }

    public static Message Decoder(byte[] baty, int type) {
        Class<? extends Message> classs = Message.class;

        switch (type) {
            case MsgType.MsgVote:
                classs = MsgVote.MsgVoteReq.class;
                break;
            case MsgType.MsgPreVote:
                classs = MsgVote.MsgVoteReq.class;
                break;
            case MsgType.MsgVoteResp:
                classs = MsgVote.MsgVoteResp.class;
                break;
            case MsgType.MsgPreVoteResp:
                classs = MsgVote.MsgVoteResp.class;
                break;

        }

        return new Gson().fromJson(new String(baty, CharsetUtil.UTF_8), classs);

    }
}
