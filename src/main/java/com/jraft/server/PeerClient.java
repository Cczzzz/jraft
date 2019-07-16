package com.jraft.server;

import com.jraft.Message.Message;
import com.jraft.Message.MsgType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

/**
 * @author chenchang
 * @date 2019/6/25 21:13
 */
public class PeerClient implements java.io.Closeable {
    Bootstrap client;

    public PeerClient() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new HttpPipelineInitializer(true, new clenthandler()));
        ChannelFuture connect = b.connect("127.0.0.1", 9999).sync().addListener(
                f -> {
                    if (f.isSuccess()) {
                        System.out.println("链接成功");
                    }
                }
        );
        for (int i = 0; i < 100; i++) {
//            ByteBuf byteBuf = Unpooled.wrappedBuffer("hi2!ddddddddddddddd".getBytes(CharsetUtil.UTF_8));
//            System.out.println(byteBuf.refCnt());
//            FullHttpRequest request = new DefaultFullHttpRequest(
//                    HttpVersion.HTTP_1_1,
//                    HttpMethod.GET,
//                    "/aaa",
//                   byteBuf);
//            request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
//            request.headers().set(HttpHeaderNames.CONTENT_LENGTH,
//                    request.content().readableBytes());
            Message message = new Message();
            message.setType(MsgType.MsgApp);

            connect.channel().writeAndFlush(message).sync();
            Thread.sleep(1000);
        }
        connect.channel().closeFuture().sync();
    }

    @Override
    public void close() throws IOException {

    }

    public static class clenthandler extends SimpleChannelInboundHandler<Message> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
            System.out.println("clent 收到消息" + msg);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        new PeerClient();
    }

}
