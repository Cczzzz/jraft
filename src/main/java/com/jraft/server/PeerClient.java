package com.jraft.server;

import com.jraft.Message.Message;
import com.jraft.node.Node;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenchang
 * @date 2019/6/25 21:13
 */
public class PeerClient implements java.io.Closeable {

    private Bootstrap client;
    private static Map<Integer, InetSocketAddress> socketAddressMap = new HashMap<>();
    private final static Map<Integer, Channel> contextMap = new ConcurrentHashMap<>();


    public PeerClient(Node node, Map<Integer, InetSocketAddress> addressMap) {
        EventLoopGroup group = new NioEventLoopGroup(addressMap.size());
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new HttpPipelineInitializer(true, new ClentHandler(node)));
        this.client = b;
        socketAddressMap = addressMap;
    }

    public void send(Integer id, Object o) throws Throwable {
        InetSocketAddress inetSocketAddress = socketAddressMap.get(id);

        Channel channel = null;
        if (contextMap.get(id) == null) {
            ChannelFuture sync = client.connect(inetSocketAddress).sync();

            if (sync.isSuccess()) {
                contextMap.put(id, sync.channel());
                channel = sync.channel();
            } else {
                //  链接失败
                throw sync.cause();
            }
        } else {
            channel = contextMap.get(id);

        }
        channel.writeAndFlush(o);
    }


    @Override
    public void close() throws IOException {
        //client.
    }

    /**
     * 根据ip计算id
     *
     * @param address
     * @return
     */
    private static int calcId(SocketAddress address) {
        InetSocketAddress socketAddress = (InetSocketAddress) address;
        return socketAddress.toString().hashCode();
    }

    private static int addChannel(SocketAddress address, Channel channel) {
        int id = calcId(address);
        contextMap.put(id, channel);
        return id;
    }

    private static int removeChannel(SocketAddress address) {
        int id = calcId(address);
        contextMap.remove(id);
        return id;
    }

    @ChannelHandler.Sharable
    public static class ClentHandler extends SimpleChannelInboundHandler<Message> {
        private Node node;

        public ClentHandler(Node node) {
            this.node = node;
        }

        private static final Logger LOGGER = LoggerFactory.getLogger(ClentHandler.class);

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            SocketAddress address = ctx.channel().remoteAddress();
            int id = addChannel(address, ctx.channel());
            LOGGER.info("连接建立: id:%d 地址:%s", id, address);
            super.channelActive(ctx);
        }

        protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
            node.getIn().send(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            SocketAddress address = ctx.channel().remoteAddress();
            LOGGER.warn("发送错误: id:%d 地址:%s ", calcId(address), address, cause);
            super.exceptionCaught(ctx, cause);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            SocketAddress address = ctx.channel().remoteAddress();
            int id = removeChannel(address);
            LOGGER.warn("连接关闭: id:%d 地址:%s", id, address);
            super.channelInactive(ctx);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        //  new PeerClient();
    }

}
