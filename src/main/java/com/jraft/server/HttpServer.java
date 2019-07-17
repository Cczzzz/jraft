package com.jraft.server;

import com.jraft.Config;
import com.jraft.Message.Message;
import com.jraft.node.Node;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

/**
 * @author chenchang
 * @date 2019/6/25 21:13
 */
public class HttpServer {

    private Config config;

    private ServerBootstrap serverBootstrap;

    private Node node;

    public HttpServer(Config config, Node node) {
        this.config = config;
        this.node = node;
    }

    /**
     * netty默认线程数
     */
    private final static int defaultEventLoopThreads = Math.max(1, SystemPropertyUtil.getInt(
            "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));


    public ChannelFuture start() throws InterruptedException {
        return start(config.getLocalPort(), 1, defaultEventLoopThreads, node);
    }

    public ChannelFuture start(int port, int bossThreads, int workThreads, Node node) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        NioEventLoopGroup work = new NioEventLoopGroup(workThreads, r -> {
            Thread thread = new Thread(r);
            thread.setName("eventLoopThread");
            return thread;
        });
        NioEventLoopGroup boss = new NioEventLoopGroup(bossThreads);

        serverBootstrap.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .childHandler(new HttpPipelineInitializer(false, new serverHandler(node)));

        ChannelFuture future = serverBootstrap.bind().sync();
        this.serverBootstrap = serverBootstrap;
        return future;
    }


    @ChannelHandler.Sharable
    public static class serverHandler extends SimpleChannelInboundHandler<Message> {
        private Node node;

        public serverHandler(Node node) {
            this.node = node;
        }

        protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
            node.getIn().send(msg);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String[] peerAddress = new String[]{"127.0.0.1:8888", "127.0.0.1:9999"};
        Config config = Config.newBuilder()
                .localPort(9999)
                .peerSocketAddresses(peerAddress)
                .build();
        HttpServer server = new HttpServer(config, null);
        server.start();
    }


}
